# setwd('C:/Users/Alex/Desktop/prog_proj/bullethell')

require(reshape2)
require(plyr)
require(MASS)
require(optimx)

source('./gpFn.R')

usr_data = read.csv('./database.csv')

configs = read.table(file='clientSettings.config', sep='=')
al_mode = read.table(file='r_configs.config', sep='=')
learn_mode = al_mode[al_mode$V1=='learn_mode',2]

# decrement as always pID is incremented after loading, so newest player is one less than stored
pID = as.numeric(as.character(configs[configs$V1=='player_id',2]))-1 

## keep only data from this user
usr_data = usr_data[as.numeric(as.character(usr_data$pID)) == pID,]

if (nrow(usr_data) > 1) {
  ## ticker to track progress
  load('r_iter.RData')
  iter = iter+1
  

  
  #### GP preference version ####
  
  if (learn_mode == 'preference') {
    ## read in pairwise preference data
    usr_comp = usr_data[c('s_wave', 'c_choice')]
    usr_pair = list()
    for (i in 2:nrow(usr_comp)) {
      if (usr_comp$c_choice[i]=='BETTER') {
        usr_pair[[i]] = c(usr_comp$s_wave[i], usr_comp$s_wave[i-1])
      } else if (usr_comp$c_choice[i]=='WORSE') {
        usr_pair[[i]] = c(usr_comp$s_wave[i-1], usr_comp$s_wave[i])
      } 
      # ignore 'NONE' cases
    }
    usr_pair = ldply(usr_pair) # change to data frame
    
    measure_var = c('s_wave', 'player.move.thrust', 'player.move.drag')
    usr_sample = usr_data[measure_var] # note: need to match up identical values at different waves
    
    # convert to set of unique samples + label them
    uniq_sample = usr_sample
    uniq_sample$s_wave = NULL
    uniq_sample = unique(uniq_sample)
    uniq_sample$label = factor(seq(1, nrow(uniq_sample)))
    
    # map unique sample labels to original waves
    usr_sample = merge(usr_sample, uniq_sample)
    wave_label = usr_sample[,c('s_wave', 'label')]
    
    # convert wave-based pair comparisons to sample-based
    usr_pair = merge(usr_pair, wave_label, by.x=c('V1'), by.y=c('s_wave'))
    usr_pair = merge(usr_pair, wave_label, by.x=c('V2'), by.y=c('s_wave'), suffixes=c('.1', '.2'))
    
    # clean up useless variables
    usr_pair$V1 = NULL
    usr_pair$V2 = NULL
    
    
    control_var = c('label', 'player.move.thrust')
    x_sample = arrange(unique(usr_sample[control_var]), label)
    x_sample$label = NULL # remove IDs
    x_sample = as.matrix(x_sample, ncol=ncol(x_sample))
    
    x_class = usr_pair
    sigma_n = 0.05
    
    # create grid of hyperparameters to search
    lengthscale_grid = matrix(rep(seq(0.001, 0.005, 0.001),ncol(x_sample)), ncol=ncol(x_sample))
    sigma_grid = seq(0.0005, 0.005, 0.0005)
    
    # optimize hyperparameters
    optmodel = optimizeHyper(hypmethod='BFGS', optmethod='Nelder-Mead', lengthscale_grid, sigma_grid, x_sample, x_class, infPrefLaplace, mean.const, kernel.SqExpND)
    
    t_pts = sort(union(unique(x_class[,1]),unique(x_class[,2])))
    t_class = expand.grid(t_pts, t_pts)
    
    t_pred = prefPredict(optmodel, t_class, x_sample, optmodel$f_map, optmodel$W, optmodel$K, optmodel$sigma_n, kernel.SqExpND, optmodel$lenscale)
    plot(t_pred$pred)
    
    ## last point tested to compare against
    n_train = nrow(x_class)
    last_pt = setdiff(x_class[n_train,], 
                      intersect(x_class[n_train-1,], x_class[n_train,]))
    if (sum(dim(last_pt))==0) {
      ## catch case where last point used same parameters twice in a row
      last_pt = x_class[n_train,1]
    }
    last_pt = as.numeric(as.character(last_pt))
    t_class[,1] = as.numeric(as.character(t_class[,1]))
    t_class[,2] = as.numeric(as.character(t_class[,2]))
    t_idx = which(t_class[,1]==last_pt | t_class[,2]==last_pt) # all possible pairs that will test using the most recent point
    t_pairs = t_class[t_idx,]
    t_pairs = t_pairs[t_pairs!=last_pt] # only keep other point to test against
    t_pairs = as.matrix(t_pairs, ncol=1)
    t_pairs = unique(t_pairs) # remove redundant
    
    f_t = optmodel$f_map[t_pairs,]
    f_plus = max(optmodel$f_map)
    
    ## pick point that optimizes objective fn
    next_sample = al.maxExpectedImprovement(optmodel$f_map, f_t, as.matrix(optmodel$sigma_n, ncol=1), t_pairs, sigma_n, slack=0.1, iter)
    next_sample = x_sample[next_sample]
    next_sample = matrix(next_sample, ncol=ncol(x_sample))
    
    ## write to control vector
    new_vec = paste(
      paste('player.move.thrust#Amount of control thrust.#0.0#0.09', round(next_sample[,1],3), sep='#'),
#       paste('player.move.drag#Amount of air drag.#0.0#1.0', round(next_sample[,2],3), sep='#'),
      'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#',
      sep='#'
    )
    write(new_vec, 'geneText.txt')
    
    print('preference learning')
  }
  
  
  #### GP regression version ####
  
  if (learn_mode == 'regression') {
    control_var = c('player.move.thrust', 'player.move.drag')
    target_var = c('s_hit_player')
    
    x = usr_data[control_var]
    x = as.matrix(x, ncol=length(control_var))
    
    tar_hit = configs[configs[,1]=='tar_hit_rate',2]
    if (length(tar_hit) < 1) {
      tar_hit = 2 # default value
    } else {
      tar_hit = as.numeric(as.character(tar_hit))
    }
    
    y = usr_data[target_var]
    y = -(y - tar_hit)^2 # - or +?
    y = as.matrix(y, ncol=length(target_var))
    
    
    ## example
    n_pts = 10
    thrust = c(0, 0.09)
    drag = c(0, 1)
    x_star_thrust = matrix(seq(thrust[1] + thrust[2]/n_pts, thrust[2]-thrust[2]/n_pts, len=n_pts), ncol=1)
    x_star_drag = matrix(seq(drag[1] + drag[2]/n_pts, drag[2]-drag[2]/n_pts, len=n_pts), ncol=1)
#     x_star_drag = 0.5
    x_star = expand.grid(x_star_thrust, x_star_drag)
    
    sigma_n = 0.05

    
    ## optimize hyperparameters
    optimx_param = optimx(par=c(1,0.5,0.5), 
                          fn=predictGP.optimx, gr=NULL, hess=NULL,
                          lower=1e-3, upper=1e3,
#                           method='Nelder-Mead',
                          method='L-BFGS-B',
                          itnmax=NULL, hessian=FALSE, 
                          control=list(trace=6),
#                           control=list(trace=10, all.methods=TRUE),
                          xin=x, yin=y, x_test=x_star, sigma_n=sigma_n, k.x_x=NULL, meanFn=mean.const, kernelF=kernel.SqExpND
    )
#     tmp = optimx_param
#     optimx_param = optimx_param[optimx_param$method=='spg',]
    kernelpars = unlist(optimx_param$par)
    
    ## predict latent functions at test points
    #   sigma_n = optimx_param$par[1]
    varscale = kernelpars[1]
    lenscale = kernelpars[2:length(kernelpars)]
    gp.pred = predictGP(x, y, x_star, sigma_n, NULL, mean.const, kernel.SqExpND, length_scale=lenscale, variance_scale=varscale^0.5)
    
    
    # sample from GP to get illustrate functions
    gp.sample = sampleGP(50, x_star, gp.pred$f.star, gp.pred$fs.cov)
    
    
    ## plot results for debug
    f_star = data.frame(x=x_star_thrust, y=gp.pred$f.star)
    names(f_star) = c('xs', 'ys')
    
    f = data.frame(x=x[,1], y=y)
    names(f) = c('x', 'y')
    
    
    
    ## greatest expected improvement among test points
    next_sample = al.maxExpectedImprovement(gp.pred$f.map, gp.pred$f.star, gp.pred$fs.cov, x_star, sigma_n, slack=0.1, iter)
    
    ## write to control vector
    new_vec = paste(
      paste('player.move.thrust#Amount of control thrust.#0.0#0.09', round(next_sample[,1],3), sep='#'),
      paste('player.move.drag#Amount of air drag.#0.0#1.0', round(next_sample[,2],3), sep='#'),
      'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#',
      sep='#'
    )
    write(new_vec, 'geneText.txt')
    
    ## debug 2D contour plot
    png(paste('f_fit_', iter, '.png', sep=''))
#     print(
#       ggplot(gp.sample, aes(x=x.Var1,y=value)) + 
#         geom_line(aes(group=variable), colour="grey80") +
#         geom_line(data=f_star,aes(x=xs,y=ys),colour="red", size=0.5) + 
#         geom_errorbar(data=f,aes(x=x,y=NULL,ymin=y-2*sigma_n, ymax=y+2*sigma_n), width=0.002) +
#         geom_point(data=f,aes(x=x,y=y)) +
#         theme_bw() +
#         xlab("input, x")
#     )
    print(
      ggplot(gp.sample, aes(x.Var1, x.Var2, z=value)) + stat_contour(geom='polygon', aes(group=variable, fill=..level..), bins=3)  + geom_point(data=next_sample, aes(x=Var1, y=Var2, z=1), size=5, colour='orange') + theme_bw()
      )
    dev.off()
    
    print('regression learning')
  }

  save(iter, file='r_iter.RData')
  
  
  
  print(paste('testing: ', next_sample))
}