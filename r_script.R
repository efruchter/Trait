# setwd('C:/Users/Alex/Desktop/prog_proj/bullethell')

require(reshape2)
require(plyr)
require(MASS)
require(optimx)

source('./gpFn.R')
javaDebug = function(msg, debug=FALSE) {
  if (debug)
    print(msg)
}

usr_data = read.csv('./database.csv')

configs = read.table(file='clientSettings.config', sep='=')
learn_mode = configs[configs$V1=='learn_mode',2]
debug_mode = configs[configs$V1=='debug_mode',2]
debug_mode = as.numeric(as.character(debug_mode))

learn_params = read.csv('./learn_params.csv')

# decrement as always pID is incremented after loading, so newest player is one less than stored
pID = as.numeric(as.character(configs[configs$V1=='player_id',2]))-1 

## keep only data from this user
usr_data = usr_data[as.numeric(as.character(usr_data$pID)) == pID,]

javaDebug("got user data", debug_mode)

if (nrow(usr_data) > 1) {
  
  ## ticker to track progress per player
  if (length(dir('./', paste('r_iter_p', pID, '.RData', sep=''))) == 0) { 
    iter=0
  } else {
    load(paste('r_iter_p', pID, '.RData', sep=''))
  }
  iter = iter+1
  

  
  #### GP preference version ####
  
  if (learn_mode == 'preference' & iter>2) {

    ## TODO: make this scale to increase sampling density as needed -> optimize iteratively
    
    javaDebug("doing preference learning", debug_mode)
    
    ## specify parameter ranges to adjust
    npts=10
    
    ## construct test point grid
    tpts = list()
    for (i in 1:nrow(learn_params)) {
      tpts[[as.character(learn_params$param[i])]] = paramGrid(npts, learn_params$min[i], learn_params$max[i])
    }
    tpts = expand.grid(tpts)

    ## construct test point labels and pairs
    tclass = cbind(seq(1:nrow(tpts)), tpts)
    names(tclass)[1] = 'label'
    
    ## label training samples & construct pairs compared
#     control_var = c('player.move.thrust', 'player.move.drag', 'c_choice')
    control_var = c(as.character(learn_params$param), 'c_choice')
    train_data = usr_data[control_var]
    train_data$pref = rep(0, nrow(train_data))
    train_data$pref[train_data$c_choice=='BETTER'] = 1
    train_data$pref[train_data$c_choice=='WORSE'] = -1
    train_data$c_choice = NULL
    
    x_sample = merge(tclass, train_data)
    x_class = cbind(x_sample$label[1:(nrow(x_sample)-1)], x_sample$label[2:(nrow(x_sample))], x_sample$pref[2:nrow(x_sample)]) # get labeled pairs with preference value in third column
    
    x_sample$pref=NULL
    x_sample = unique(x_sample) # only distinct points
    x_sample = as.matrix(x_sample)
    
    # reorder columns for label in first
    xs_dim = ncol(x_sample)-1
    x_sample = x_sample[,c(xs_dim+1,seq(1:xs_dim))]
    
    javaDebug('read samples', debug_mode)
    
    ## get last point to compare against
    n_train = nrow(x_class)
    last_pt = setdiff(x_class[n_train,], 
                      intersect(x_class[n_train-1,], x_class[n_train,]))
    last_pt = last_pt[1]
    
    ## construct test pairs
    tclass_pair = expand.grid(last_pt, tclass$label)
    tclass_pair = subset(tclass_pair, tclass_pair[,2] != last_pt) # don't compare to same point again
    tclass_pair = as.matrix(tclass_pair)
    
    # create grid of hyperparameters to search
    lengthscale_grid = matrix(rep(seq(0.01, 0.1, length.out=20),ncol(x_sample)-1), ncol=ncol(x_sample)-1)
    sigma_grid = seq(0.0005, 0.5, length.out=10)
    
    ## only optimize hyper parameters every 3 iterations
    if (iter %% 3 == 0 & iter > 0) {
      # optimize hyperparameters + generate inferences
      optmodel = optimizeHyper(hypmethod='BFGS', optmethod='Nelder-Mead', lengthscale_grid, sigma_grid, x_sample, x_class, infPrefLaplace, mean.const, kernel.SqExpND)
      save(optmodel, file=paste('optHyper_p', pID, '.RData', sep=''))
      
      
    } else {
      # load previously optimized hyperparameters
      load(paste('optHyper_p', pID, '.RData', sep=''))
      
      # update model inferences for new data
      tbest = infPrefLaplace(x_sample, x_class, mean.const, kernel.SqExpND, tol=1e-06, max_iter=100, sigma_n=optmodel$sigma_n, optmethod='Nelder-Mead', optmodel$lenscale)
      optmodel$f_map = tbest$f_map
      optmodel$W = tbest$p_map$liks$d2lp
      optmodel$K = tbest$K
      
      javaDebug('loaded hypers', debug_mode)
    }
    
    ## predictive preference probability for 2nd over 1st in pair
    t_pred = prefPredict.v2(optmodel, tclass_pair, tclass, x_sample, optmodel$f_map, optmodel$W, optmodel$K, optmodel$sigma_n, kernel.SqExpND, optmodel$lenscale)
    plot(t_pred$pred)
    
    javaDebug('generated predictions', debug_mode)

    # (1) look up predictive means for each sample value
    f_t = t_pred$mu_s[,2] # second column are new values to compare to
    # (2) look up predictive variance
    sigma_t = diag(t_pred$sigma_s)
    # (3) collect up sample values for test points
    test_pts = tclass[tclass$label!=last_pt,-1]
    # (4) evaluate point to try next
    next_sample = al.maxExpectedImprovement.v2(optmodel$f_map, f_t, sigma_t, test_pts, slack=0.1, iter)
    next_sample = as.matrix(next_sample, ncol=ncol(next_sample))
    
    javaDebug('selected sample', debug_mode)
    
    ## write to control vector
#     new_vec = paste(
#       paste('player.move.thrust#Amount of control thrust.#0.0#0.09', round(next_sample[,1],4), sep='#'),
#       paste('player.move.drag#Amount of air drag.#0.0#1.0', round(next_sample[,2],4), sep='#'),
#       'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#',
#       sep='#'
#     )
    new_vec = paste(
      paste(learn_params$param, '', learn_params$min, learn_params$max, next_sample, sep='#', collapse='#'),
      'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#',
      sep='#'
    )
    write(new_vec, 'geneText.txt')
    
    print('preference learning')
  }
  
  
  #### GP regression version ####
  
  ## TODO: update to read in parameters to tweak
  
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

  save(iter, file=paste('r_iter_p', pID, '.RData', sep=''))
  
  
  
#   print(paste('testing: ', next_sample))
}