# setwd('C:/Users/Alex/Desktop/prog_proj/bullethell_merge')
# setwd('./Desktop/prog_proj/bullethell/')

require(reshape2)
require(plyr)
require(MASS)
require(optimx)

#### code for running aspects of the learning ####
source('./gpFn.R')

## writes out gene vector to text file
writeGene = function(next_sample, learn_params, fname, learn_mode) {
## write to control vector
if (learn_mode == 'preference' | learn_mode == 'preference+random') {
  new_vec = paste(
    paste(learn_params$param, '', learn_params$min, learn_params$max, next_sample, sep='#', collapse='#'),
    'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#',
    sep='#'
  )
}
else if (learn_mode == 'regression' | learn_mode == 'regression+random') {
  new_vec = paste(
    paste(learn_params$param, '', learn_params$min, learn_params$max, next_sample, sep='#', collapse='#'),
    'player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#player.move.thrust##0.0#0.09#0.04#player.move.drag##0.0#1.0#0.4#',
    sep='#'
  )
}

write(new_vec, fname)
}


cat('called R \n')

#### read in configuration and data for this player ####

usr_data = read.csv('./database.csv')

cat('user data read', dim(usr_data), '\n')

configs = read.table(file='clientSettings.config', sep='=')

cat('configs read \n')


# command line arguments:
inArgs = commandArgs()
arg_idx = which(inArgs == '--args')
arg_list = inArgs[(arg_idx+1):length(inArgs)]

cat('args read\n', inArgs, '\n')

pID = as.numeric(arg_list[1]) # first argument is player ID

cat('got pid: ', pID, '\n') 

learn_mode = arg_list[2]
cat('learning mode: ', learn_mode, '\n')

if (learn_mode == 'preference' | learn_mode == 'preference+random') {
  learn_params = read.csv('./learn_params_pref.csv')
} else if (learn_mode == 'regression' | learn_mode == 'regression+random') {
  learn_params = read.csv('./learn_params_reg.csv')
} else {
  cat('error in learning mode: no learning parameters!\n')
}

cat('learn_params read \n')

cat('raw iteration data: ', arg_list[3], '\n')
iter = as.numeric(arg_list[3])
cat('iteration: ', iter, '\n')

cat('getting user data \n')

## keep only data from this user
usr_data = usr_data[as.numeric(as.character(usr_data$pID)) == pID,]


cat('got user data', dim(usr_data),  '\n')

#### running learning process ####


#### random sampling ####

if (learn_mode == 'preference+random' | learn_mode == 'regression+random') {
  
  if (iter < 2 & learn_mode == 'regression+random') {
    cat('fixed iteration: ', iter, '\n')
    
    new_vec = 'player.move.thrust#Amount of control thrust.#0.0#0.09#0.040#player.move.drag#Amount of air drag.#0.0#1.0#0.3#player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#'
    write(new_vec, 'geneText.txt')
    
  }
  
  cat('random sampling: ', learn_mode, '\n')
  
  next_sample = randomPts(1, learn_params)
  
  cat('next point:\n', paste(learn_params$param, '', learn_params$min, learn_params$max, next_sample, sep=' ', collapse='\n'), '\n')
  
  writeGene(next_sample, learn_params, 'geneText.txt', learn_mode)
}


#### GP preference version ####

if (learn_mode == 'preference') {
  
  cat('preference learning \n')
  
  if (iter < 4) {
    
    cat('fixed iteration: ', iter, '\n')
    
    ## first 3 iterations use fixed examples
    if (iter < 2) {
      new_vec = 'player.move.thrust#Amount of control thrust.#0.0#0.09#0.050#player.move.drag#Amount of air drag.#0.0#1.0#0.28#player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#'  
    } else if (iter == 2) {
      new_vec = 'player.move.thrust#Amount of control thrust.#0.0#0.09#0.022#player.move.drag#Amount of air drag.#0.0#1.0#0.28#player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#
'
    } else if (iter == 3) {
      new_vec = 'player.move.thrust#Amount of control thrust.#0.0#0.09#0.042#player.move.drag#Amount of air drag.#0.0#1.0#0.82#player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#
'
    }
    
    write(new_vec, 'geneText.txt')
    
  } else {
    
    ## TODO: make this scale to increase sampling density as needed -> optimize iteratively
    
    cat('learning iteration \n')
    
    ## specify number test points per range
    ndrop = 10 # number of recently tested samples to not reuse
    
    ## construct test point grid
    tpts = testGrid(learn_params)
    
    cat('made test point grid \n')

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
    train_data = subset(train_data, train_data$pref != 0) # remove "NONE" choices
    
    ## construct test point labels and pairs
    tclass = rbind(train_data[as.character(learn_params$param)], tpts)
    tclass = unique(tclass)
    tclass = cbind(1:nrow(tclass), tclass)
    names(tclass)[1] = 'label'
    
    
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
    
    cat('unique sample dimensionality: ', dim(x_sample), '\n')
    
    ## construct test pairs
    tclass_pair = expand.grid(last_pt, tclass$label)
    tclass_pair = subset(tclass_pair, !(tclass_pair[,2] %in% last_Npt)) # don't reuse recent points
    tclass_pair = as.matrix(tclass_pair)
    
    # create grid of hyperparameters to search
    lengthscale_grid = matrix(rep(seq(0.01, 0.1, length.out=20),ncol(x_sample)-1), ncol=ncol(x_sample)-1)
    sigma_grid = seq(0.0005, 0.5, length.out=10)
    
    cat('constructed points to use \n')
    
    optmodel = optimizeHyper(hypmethod='BFGS', optmethod='Nelder-Mead', lengthscale_grid, sigma_grid, x_sample, x_class, infPrefLaplace, mean.const, kernel.SqExpND)
    
    cat('optimized hyperparameters \n')

    ## predictive preference probability for 2nd over 1st in pair
    t_pred = prefPredict.v2(optmodel, tclass_pair, tclass, x_sample, optmodel$f_map, optmodel$W, optmodel$K, optmodel$sigma_n, kernel.SqExpND, optmodel$lenscale)
    plot(-t_pred$pred)
    
    cat('generated predictions \n')
    
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
    
    cat('predicted sample \n')
    
    cat('testing:\n', paste(as.character(learn_params$param), next_sample, sep=': ', collapse='\n'), '\n')
    
    writeGene(next_sample, learn_params, 'geneText.txt', learn_mode)
    
    
    debug_data = c(pID, iter, nrow(usr_data), next_sample, optmodel$sigma_n, optmodel$lenscale)
    names(debug_data) = c('pID','iter','nsamples', as.character(learn_params$param), 'sigma_n', paste('lenscale', 1:length(optmodel$lenscale), sep=''))
    write(debug_data, 'pref_debug.csv', sep=',', append=TRUE)
  }
}


#### GP regression version ####
  
if (learn_mode == 'regression') {
  
  cat('doing regression \n')
  
  if (iter < 2) {
    cat('fixed iteration: ', iter, '\n')
    
    new_vec = 'player.move.thrust#Amount of control thrust.#0.0#0.09#0.040#player.move.drag#Amount of air drag.#0.0#1.0#0.3#player.radius.radius#Player ship radius#2.0#50.0#10.0#spawner.enemy.radius.c0#Base enemy radius.[0]#10.0#20.0#10.0#spawner.enemy.radius.c1#Base enemy radius.[1]#10.0#20.0#10.0#enemy.bullet.speed#Speed of enemy bullets.#0.0#3.0#0.8#enemy.bullet.size#Size of enemy bullets.#0#80.0#10.0#enemy.bullet.damage#Damage of enemy bullets.#0#100.0#5.0#enemy.bullet.cooldown#Cooldown time between firing enemy bullets.#0#1000.0#500.0#'
    write(new_vec, 'geneText.txt')
    
  } else {
    
    cat('learning iteration \n')

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
    y = as.numeric(as.character(y[,1]))
    y = -(y - tar_hit)^2 # - or +?
    y = as.matrix(y, ncol=length(target_var))
    
    cat('read in data \n')
    
    ## construct test point grid
    x_star = testGrid(learn_params)
    sigma_n = 0.05
    
    cat('made test points \n')
    cat('test point dimensions: ', dim(x_star), ' \n')
    cat('training x dimensions: ', dim(x), ' \n')
    cat('training y dimensions: ', dim(y), ' \n')
    
    require(optimx)
    
    ## optimize hyperparameters
    optim_inpar = c(1, rep(0.5, ncol(x_star))) # initial parameters are 1 for variance scale, 0.5 for all length scales
    optimx_param = optimx(par=optim_inpar, 
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
    
    cat('optimized hypers \n')
    
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
    x_all = rbind(x, x_star)
    x_all = unique(x_all)
    x_star_lab = cbind(label=1:nrow(x_all), x_all)
#     x_star_lab = cbind(label=1:nrow(x_star), x_star)
    x2 = usr_data[c('s_wave', control_var)]
    x2 = x2[max(1, nsample-ndrop):nsample,]
    x2$s_wave = NULL
    x_lab = merge(x2, x_star_lab)
    match_idx = !(x_star_lab$label %in% x_lab$label)
    
    cat('setup samples \n')
    
    ## greatest expected improvement among test points
    next_sample = al.maxExpectedImprovement.v2(gp.pred$f.map, gp.pred$f.star[match_idx], diag(gp.pred$fs.cov)[match_idx], x_star[match_idx,], slack=0.1)
    next_sample = as.matrix(next_sample, ncol=ncol(next_sample))
    
    cat('got optimal next \n')
    
    writeGene(next_sample, learn_params, 'geneText.txt', learn_mode)
    
    cat('testing:\n', paste(as.character(learn_params$param), next_sample, sep=': ', collapse='\n'), '\n')
    cat('wrote gene \n')
    
    debug_data = c(pID, iter, nrow(usr_data), next_sample, varscale, lenscale)
    names(debug_data) = c('pID','iter','nsamples', as.character(learn_params$param), 'varscale', paste('lenscale', 1:length(lenscale), sep=''))
    write(debug_data, 'reg_debug.csv', sep=',', append=TRUE)
  }
}

#   cat('saving iteration \n')
#   save(iter, file=paste('r_iter_p', pID, '.RData', sep=''))

cat('all done \n')
