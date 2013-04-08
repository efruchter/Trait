# setwd('C:/Users/Alex/Desktop/prog_proj/bullethell')

require(reshape2)
require(plyr)
require(MASS)
require(optimx)

#### code for running aspects of the learning ####
source('./gpFn.R')

## prints out debug statements when in debug mode
javaDebug = function(msg, debug=FALSE) {
  if (debug)
    print(msg)
}

## writes out gene vector to text file
writeGene = function(next_sample, learn_params, fname) {
  ## write to control vector
  new_vec = paste(
    paste(learn_params$param, '', learn_params$min, learn_params$max, next_sample, sep='#', collapse='#'),
    'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#',
    sep='#'
  )
  write(new_vec, fname)
}




#### read in configuration and data for this player ####

usr_data = read.csv('./database.csv')

configs = read.table(file='clientSettings.config', sep='=')
# learn_mode = configs[configs$V1=='learn_mode',2]
# debug_mode = configs[configs$V1=='debug_mode',2]
# debug_mode = as.numeric(as.character(debug_mode))

learn_params = read.csv('./learn_params.csv')

# command line arguments:
inArgs = commandArgs()
arg_idx = which(inArgs == '--args')
arg_list = inArgs[(arg_idx+1):length(inArgs)]

# decrement as always pID is incremented after loading, so newest player is one less than stored
# pID = as.numeric(as.character(configs[configs$V1=='player_id',2]))-1 

pID = as.numeric(arg_list[1]) # first argument is player ID
# print(paste('got player ID: ', pID, sep=''))

learn_mode = arg_list[2]
# print(paste('got learning mode: ', learn_mode, sep=''))

debug_mode = as.numeric(arg_list[3])
# print(paste('got debug mode: ', debug_mode, sep=''))

## keep only data from this user
usr_data = usr_data[as.numeric(as.character(usr_data$pID)) == pID,]

javaDebug("got user data", debug_mode)


#### running learning process ####

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
    
    ## specify number test points per range
    npts = 10
    ndrop = 10 # number of recently tested samples to not reuse
    
    ## construct test point grid
    tpts = testGrid(npts, learn_params)

    ## construct test point labels and pairs
    tclass = cbind(seq(1:nrow(tpts)), tpts)
    names(tclass)[1] = 'label'
    
    ## label training samples & construct pairs compared
    control_var = c(as.character(learn_params$param), 'c_choice', 's_wave')
    train_data = usr_data[control_var]
    train_data$pref = rep(0, nrow(train_data))
    train_data$pref[train_data$c_choice=='BETTER'] = 1
    train_data$pref[train_data$c_choice=='WORSE'] = -1
    train_data$c_choice = NULL
    
    x_sample = merge(tclass, train_data)
    x_sample = arrange(x_sample, s_wave)
    
    ## get last point to compare test points against
    ## also last 10 points to remove from possible tests
    nsample = nrow(x_sample)
    last_Npt = x_sample[max(1,(nsample-ndrop)):nsample,]
    last_Npt = as.numeric(as.character(last_Npt$label))
    last_pt = last_Npt[length(last_Npt)]
    
    ## construct all pairs of training points
    x_class = cbind(x_sample$label[1:(nrow(x_sample)-1)], x_sample$label[2:(nrow(x_sample))], x_sample$pref[2:nrow(x_sample)]) # get labeled pairs with preference value in third column
    
    ## clean up labels for samples
    x_sample$pref=NULL
    x_sample$s_wave=NULL
    x_sample = unique(x_sample) # only distinct points
    x_sample = as.matrix(x_sample)
    
    # reorder columns for label in first
    xs_dim = ncol(x_sample)-1
    x_sample = x_sample[,c(xs_dim+1,1:xs_dim)]
    
    javaDebug('read samples', debug_mode)
    
    ## construct test pairs
    tclass_pair = expand.grid(last_pt, tclass$label)
#     tclass_pair = subset(tclass_pair, tclass_pair[,2] != last_pt) # don't compare to same point again
    tclass_pair = subset(tclass_pair, !(tclass_pair[,2] %in% last_Npt)) # don't reuse recent points
    tclass_pair = as.matrix(tclass_pair)
    
    # create grid of hyperparameters to search
    lengthscale_grid = matrix(rep(seq(0.01, 0.1, length.out=20),ncol(x_sample)-1), ncol=ncol(x_sample)-1)
    sigma_grid = seq(0.0005, 0.5, length.out=10)
    
    ## only optimize hyper parameters every 3 iterations
    if (iter %% 3 == 0 & iter > 0 | 
          iter == 1) {
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
    plot(-t_pred$pred)
    
    javaDebug('generated predictions', debug_mode)

    # (1) look up predictive means for each sample value
      # note: sign flip as Rasmussen swaps d2lp for -d2lp
    f_t = -t_pred$mu_s[,2] # second column are new values to compare to
    # (2) look up predictive variance
    sigma_t = diag(t_pred$sigma_s)
    # (3) collect up sample values for test points
#     test_pts = tclass[tclass$label!=last_pt,-1]
    test_pts = tclass[!(tclass$label%in%last_Npt),-1]
    # (4) evaluate point to try next
    next_sample = al.maxExpectedImprovement.v2(optmodel$f_map, f_t, sigma_t, test_pts, slack=0.1, iter)
    next_sample = as.matrix(next_sample, ncol=ncol(next_sample))
    
    javaDebug('selected sample', debug_mode)
    
    writeGene(next_sample, learn_params, 'geneText.txt')
  }
  
  
  #### GP regression version ####
    
  if (learn_mode == 'regression') {
    javaDebug('regression fitting', debug_mode)
    
    control_var = as.character(learn_params$param)
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
    
    
    ## construct test point grid
    npts = 10
    x_star = testGrid(npts, learn_params)
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
    f_star = data.frame(x=x_star[,1], y=gp.pred$f.star)
    names(f_star) = c('xs', 'ys')
    
    f = data.frame(x=x[,1], y=y)
    names(f) = c('x', 'y')
    
    
    ndrop = 10 # number previous points to drop
    
    ## find samples not used recently
    nsample = nrow(x)
    x_star_lab = cbind(label=1:nrow(x_star), x_star)
    x2 = usr_data[c('s_wave', control_var)]
    x2 = x2[max(1, nsample-ndrop):nsample,]
    x2$s_wave = NULL
    x_lab = merge(x2, x_star_lab)
    match_idx = !(x_star_lab$label %in% x_lab$label)
    
    ## greatest expected improvement among test points
    next_sample = al.maxExpectedImprovement.v2(gp.pred$f.map, gp.pred$f.star[match_idx], diag(gp.pred$fs.cov)[match_idx], x_star[match_idx,], slack=0.1)
    next_sample = as.matrix(next_sample, ncol=ncol(next_sample))
    
    writeGene(next_sample, learn_params, 'geneText.txt')


    
    ## debug 2D contour plot
#     png(paste('f_fit_', iter, '.png', sep=''))
#     print(
#       ggplot(gp.sample, aes(x.Var1, x.Var2, z=value)) + stat_contour(geom='polygon', aes(group=variable, fill=..level..), bins=3)  + geom_point(data=next_sample, aes(x=Var1, y=Var2, z=1), size=5, colour='orange') + theme_bw()
#       )
#     dev.off()
    
    javaDebug(paste('next point: ', paste(next_sample, collapse=', ')), debug_mode)
    
    javaDebug('regression learning', debug_mode)
  }

  save(iter, file=paste('r_iter_p', pID, '.RData', sep=''))
}