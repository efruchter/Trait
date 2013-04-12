require(MASS)
require(plyr)
require(reshape2)
require(ggplot2)

#### MEAN FUNCTIONS ####

## mean function with constant value, defaults to 0 mean
mean.const = function(V, const=0, deriv=FALSE) {
  mu = rep(const, nrow(V))
  
  if (deriv == TRUE) {
    derivs = list()
    derivs[[1]] = rep(1, nrow(V))
    derivs[[2]] = rep(0, nrow(V))
    
    return(list(mu=mu, derivs=derivs))
  }
  
  return(mu)
}

#### COVARIANCE FUNCTIONS ####

# N-dimensional version of squared exponential kernel.
#   V1, V2 - matrices of n X D data points
#   length_scale - vector of length scale of each dimension, size 1 X D
#   variance_scale - scalar for scaling variance
kernel.SqExpND = function(V1, V2, deriv=FALSE, length_scale, variance_scale=1) {
  if (ncol(V1) != ncol(V2)) {
    print("V1 and V2 have different dimensions!")
    return(NULL)
  }
  
  a = diag(1/length_scale, ncol=ncol(V1), nrow=ncol(V1)) %*% t(as.matrix(V1))
  b = diag(1/length_scale, ncol=ncol(V2), nrow=ncol(V2)) %*% t(as.matrix(V2))
  
  c = sq_dist(a, b)
  
  # c is squared distances
  # want K = sf2 exp(-c/2)
  K = variance_scale * exp(-c / 2)
  
  
  if(deriv) {
    derivs = list()
    # derivatives of length scales for each dimension
    for (d in 1:ncol(V1)) {
      derivs[[d]] = K * sq_dist(t(V1[,d]) / length_scale[d], t(V2[,d]) / length_scale[d])
    }
    derivs[[d+1]] = 2 * K # variance scale parameter
    return(list(K=K, derivs=derivs))
  }
  
  return(K)
}

sq_dist = function(a, b=NULL) {
  if (is.null(b)) b = a
  n = ncol(a)
  m = ncol(b)
  
  # subtract means to avoid numerical issues for nearby values
  # note: no effect on covariance
  mu = n/(n+m) * apply(a, 1, mean) + m/(n+m) * apply(b, 1, mean)
  a = a - mu
  b = b - mu
  
  atmp = t(t(apply(a*a, 2, sum)))
  a2 = matrix(rep(atmp, m), ncol=m)
  
  btmp = apply(b*b, 2, sum)
  b2 = t(matrix(rep(btmp, n), ncol=n))
  
  # (a-b)^2 = a^2 + b^2 - 2ab
  c = a2 + b2 - 2 * t(a) %*% b
  
  return(c)
}


#### UTILITY FUNCTIONS ####

# from Rasmussen Matlab code: safely compute Gaussian over cumulative gaussian
gauOverCumGauss = function(f, p) {
  ok = f > -5
  bad = f < -6
  
  n_p = matrix(0, ncol=ncol(f), nrow=nrow(f))
  n_p[ok] = exp(-f[ok]^2 / 2) / (sqrt(2*pi)) / p[ok]
  n_p[bad] = sqrt(f[bad]^2 / 4 + 1) - f[bad] / 2
  
  interp = !ok & !bad
  tmp = f[interp]
  lam = -5 - f[interp]
  n_p[interp] = (1-lam) * (exp(-tmp^2/2)/sqrt(2*pi)) / p[interp] + lam*(sqrt(tmp^2/4+1)-tmp/2)
  return(n_p)
}

## solves linear equation from a cholesky factorization
#   equation: AX = B solved for X when A is square, symmetric, positive, definite
#   L = chol(A)
solve_chol = function(L, B) {
  solve(L, solve(t(L), B))
}

## takes a set of samples with labels in the first column and extracts two vectors for 
#   sample values from a set of training pairs of labels
#   sample_pt     -   samples with labels in the first column
#   class_pt      -   pairs of sample labels to compare
unpack_samples = function(sample_pt, class_pt) {
  cpt1 = sample_pt[match(class_pt[,1], sample_pt[,1]),-1]
  cpt2 = sample_pt[match(class_pt[,2], sample_pt[,1]),-1]
  return(list(s1=cpt1, s2=cpt2))
}


#### GP REGRESSION ####

## implementation of algorithm 2.1 from Rasumussen + Williams 2006, page 19
# given data points, values, a kernel function, estimated noise, and set of test points
# return the new mean function, covariance function, and loglikelihood of a GP model of these points
# also calculates derivatives of logliklihood
# additional parameters to the kernel function are passed as the ...
computeGP = function(x, y, sigma.noise, x.test, meanFn, kernelFn, ...) {
  m = meanFn(x)
  
  k.x_x = kernelFn(x, x, ...)
  #   k.x_xs = kernelFn(x, x.test, ...)
  k.xs_x = kernelFn(x.test, x, ...)
  k.xs_xs = kernelFn(x.test, x.test, ...)
  #   k.x_x.noise = k.x_x + sigma.noise^2 * diag(1, ncol(k.x_x))
  
  
  ## cholesky decomposition used for matrix inversion as per algorithm 2.1, Rasmussen+Williams p.19
  L = t(chol(k.x_x + sigma.noise^2 * diag(1, ncol(k.x_x))))
  alpha = solve(t(L), solve(L, y-m))
  
  f.map = k.x_x %*% alpha
  f.star = k.xs_x %*% alpha
  
  ## implements Rasmussen's gpml code, which does not include full covariance matrix
  #   v_star = diag(k.xs_xs) + apply( k.x_xs * (L %*% k.x_xs), 2, sum)
  #   cov.f.star = pmax(v_star, 0) # remove numeric noise by setting to 0
  
  ## implements algorithm 2.1, Rasmussen + Williams p.19
  v = solve(L, t(k.xs_x))
  v_star = k.xs_xs - t(v) %*% v
  cov.f.star = v_star
  #   cov.f.star = pmax(v_star, 0)
  
  ## marginal log-liklihood / evidence
  lZ = -0.5 * t(y-m) %*% alpha - sum(log(diag(L))) - 0.5*nrow(x)*log(2*pi*sigma.noise)
  
  ## derviatives of marginal log-liklihood wrt hyperparameters
  Qmat = alpha %*% t(alpha) - solve_chol(L, diag(rep(1, nrow(x)))) / sigma.noise # aa^T - K^-1
  derivs = kernelFn(x, x, deriv=TRUE, ...)
  dlZ.cov = list()
  for (i in 1:length(derivs$derivs)) {
    dlZ.cov[[i]] = sum(Qmat * derivs$derivs[[i]]) / 2
  }
  
  dlZ.lik = sigma.noise * sum(diag(Qmat))
  
  dlZ.mean = meanFn(x, deriv=TRUE)
  
  return(list(f.map=f.map, f.star=f.star, f.cov=cov.f.star, f.loglik=lZ, x.train=x, y.train=y, x.test=x.test, kernelFn=kernelFn, kernelParams=list(...), covFn=k.x_x, lZ=lZ, dlZ.cov=dlZ.cov, dlZ.lik=dlZ.lik))
}

## given a set of training and test points calculate the predictive mean and covariance
#   of a GP on the training points
predictGP = function(x, y, x_test, sigma.noise, k.x_x=NULL, meanFn, kernelFn, ...) {
  m = meanFn(x)
  
  if (is.null(k.x_x)) {
    k.x_x = kernelFn(x, x, FALSE, ...)
  }
  k.xs_x = kernelFn(x_test, x, FALSE, ...)
  k.xs_xs = kernelFn(x_test, x_test, FALSE, ...)
  
  
  L = t(chol(k.x_x + sigma.noise^2 * diag(1, ncol(k.x_x))))
  alpha = solve(t(L), solve(L, y-m))
  
  ## predict means
  f.map = k.x_x %*% alpha # f means at training points
  f.star = k.xs_x %*% alpha # f meants at test points
  
  ## predict variances
  ## implements algorithm 2.1, Rasmussen + Williams p.19
  v = solve(L, t(k.xs_x))
  v_star = k.xs_xs - t(v) %*% v
  cov.f.star = v_star
  
  lZ = -0.5 * t(y-m) %*% alpha - sum(log(diag(L))) - 0.5*nrow(x)*log(2*pi*sigma.noise)
  
  ## derviatives of marginal log-liklihood wrt hyperparameters
  Qmat = alpha %*% t(alpha) - solve_chol(L, diag(rep(1, nrow(x)))) / sigma.noise # aa^T - K^-1
  derivs = kernelFn(x, x, deriv=TRUE, ...)
  dlZ.cov = list()
  for (i in 1:length(derivs$derivs)) {
    dlZ.cov[[i]] = sum(Qmat * derivs$derivs[[i]]) / 2
  }
  
  return(list(f.map=f.map, f.cov=k.x_x, f.star=f.star, fs.cov=cov.f.star, lZ=lZ, dlz.cov=dlZ.cov))
}

predictGP.score.curry = function(kernel_scale) {
  load('./optim_data.RData')
  nparam = length(kernel_scale)
  score = predictGP(params$x, params$y, params$x_test, params$sigma.noise, params$k.x_x, params$meanFn, params$kernelFn, length_scale=kernel_scale[1:(nparam-1)], variance_scale=kernel_scale[nparam])
  return(-score$lZ)
}

predictGP.gradient.curry = function(kernel_scale) {
  load('./optim_data.RData')
  nparam = length(kernel_scale)
  param_grad = predictGP(params$x, params$y, params$x_test, params$sigma.noise, params$k.x_x, params$meanFn, params$kernelFn, length_scale=kernel_scale[1:(nparam-1)], variance_scale=kernel_scale[nparam])
  ls_grad = unlist(param_grad$dlz.cov)
  return(ls_grad)
}

## return score or gradient for optimx
## mode = 'score' OR 'gradient'
predictGP.optimx = function(kernelpar, xin, yin, x_test, sigma_n, k.x_x, meanFn, kernelFn, ...) {
  ## kernel par should be: [variance scale, lengthscale[1], lengthscale[2], ..., lengthscale[D]]
  ##    where D is dimensionality of xin
  nparam = length(kernelpar)
  outparam = predictGP(x, y, x_test, sigma_n, 
                       k.x_x, meanFn, kernelFn, 
                       length_scale=kernelpar[2:nparam], variance_scale=kernelpar[1])
  return(-as.numeric(outparam$lZ))
}


predictGP.optimx.grad = function(kernelpar, xin, yin, x_test, sigma_n, k.x_x, meanFn, kernelFn, ...) {
  nparam = length(kernelpar)
  outparam = predictGP(x, y, x_test, sigma_n, k.x_x, meanFn, kernelFn, length_scale=kernelpar[1:(nparam-1)], variance_scale=kernelpar[nparam])
  return(unlist(outparam$dlz.cov))
}


## sample functions given predictive mean and covariance at a series of points
sampleGP = function(n.samples, x.test, f.mean, f.cov) {
  vals = mvrnorm(n.samples, f.mean, f.cov)
  #   vals = rnorm(GP$f.mean, GP$f.cov)
  #   vals = cbind(x = GP$x.test, vals)
  vals = cbind(x = x.test, as.data.frame(t(vals)))
  x_vars = names(vals)[grep('x', names(vals))] # find all input variable names
  vals = melt(vals, id = x_vars)
  #   vals = melt(vals, id='x')
  return(vals)
}

#### GP PREFERENCE LEARNING ####

## compute pairwise preference value for given pairs from a vector of latent values and pairs
pairPref = function(f, class_pt, sigma_noise) {
  cpts = unpack_samples(f, class_pt)
  Z = cpts$s1 - cpts$s2
#   Z = f[class_pt[,1]] - f[class_pt[,2]]
  Z = Z / (sqrt(2) * sigma_noise)
  return(Z)
}

## pairwise preference from vectors of latent values for paired objects
pairPref2Vec = function(f1, f2, sigma_noise) {
  if (length(f1) != length(f2)) {
    warning('Pairwise preferneces on vectors with unequal lengths!')
  }
  Z = f1 - f2
  Z = Z / (sqrt(2) * sigma_noise)
  return(Z)
}

## pairwise preference values (Z) given predictive means and fixed noise for all samples
prefFn = function(mu_pair, sigma_n) {
  pnorm((mu_pair[,1] - mu_pair[,2]) / (sqrt(2) * sigma_n))
}

## pairwise preference values (Z) for pairs of points with differing noise
## for each point
#   mu_pair - predictive means of test points
#   sigma_n - matrix of pairwise noise values
#   t_pair - indexes into noise matrix
#   
prefFnVec = function(mu_pair, t_pair, sigma_n) {
  
#   # unpack matrix of sigma values into points for each test case
#   sigma_vec = list()
#   for (i in 1:nrow(t_pair)) {
#     sigma_vec = append(sigma_vec, sigma_n[t_pair[i,1], t_pair[i,2]])
#   }
#   sigma_vec = unlist(sigma_vec)
  
  # unpack matrix of sigma values into points for each test case
#   sigma_list = c()
#   for (i in 1:nrow(sigma_n)) {
#     for (j in 1:ncol(sigma_n)) {
#       sigma_list = rbind(sigma_list, c(rownames(sigma_n)[i], colnames(sigma_n)[j], sigma_n[i,j]))
#     }
#   }
#   
#   sigma_vec = sigma_list[which(sigma_list[,1]==t_pair[,1] & sigma_list[,2]==t_pair[,2]),3]
#   sigma_vec = as.numeric(sigma_vec)
#   pnorm((mu_pair[,1] - mu_pair[,2]) / (sqrt(2) * sigma_vec))
  
  pnorm((mu_pair[,2] - mu_pair[,1]) / (sqrt(2) * diag(sigma_n)))
}


## likelihood function for pairwise preferences over samples
##  takes a set of provided comparisons and evaluates probability, log probability, 
##  and gradient and hessian of log probability for those sample comparisons
#     sample_pt   - features for objects compared
#     class_pt    - definition of pairs of objects to be compared; indexes into sample_pt starting at 1
#     mu          - mean of latent values for samples
#     s2          - covariance of samples
#     sigma_noise - noise in judgements of sample preferences
#     infFn       - function to perform inference 
likPref = function(sample_pt, class_pt, mu, s2, sigma_noise) {
#   f = mu
  f = cbind(sample_pt[,1], mu) # construct f to have indexing for lookup
#   Z = matrix(pairPref(f, class_pt, sigma_noise))
  Z = matrix(pairPref(f, class_pt, sigma_noise))
  p = pnorm(Z)
  lp = log(p) # note: may be unstable, cf Rasmussen's "logphi" function
  n_p = gauOverCumGauss(Z, p)
  
#   dlp = matrix(0, length(mu)) # gradient of log likelihood
#   for (i in 1:nrow(sample_pt)) {
# #   for (i in unique(sample_pt[,1])) {
#     # preferences are encoded as preferred example 1st, unpreferred 2nd
#     # weight preferences as +1, unpreferences as -1
#     pos_idx = class_pt[,1] == sample_pt[i,1]
#     neg_idx = class_pt[,2] == sample_pt[i,1]
#     dlp[i] = sum(n_p[pos_idx]) - sum(n_p[neg_idx])
#   }
  
  dlp = matrix(0, length(mu))
  for (i in 1:nrow(sample_pt)) {
    ## find rows that use this training sample
    s_1_row = class_pt[,1] == sample_pt[i,1]
    s_2_row = class_pt[,2] == sample_pt[i,1]
    
    ## find which are positive preference, which are negative
    pos_idx = (s_1_row & class_pt[,3] == -1) | (s_2_row & class_pt[,3] == 1)
    neg_idx = (s_1_row & class_pt[,3] == 1) | (s_2_row & class_pt[,3] == -1)
    dlp[i] = sum(n_p[pos_idx]) - sum(n_p[neg_idx])
  }
  
#   d2lp = matrix(0, length(mu), length(mu)) # Hessian of log likelihood
#   for (i in 1:nrow(sample_pt)) {
# #   for (i in unique(sample_pt[,1])) {
#     for (j in 1:nrow(sample_pt)) {
# #     for (j in unique(sample_pt[,2])) {
#       # preferences are encoded as preferred example 1st, unpreferred 2nd
#       # weight preferences as +1, unpreferences as -1
#       pos_idx = class_pt[,1] == sample_pt[i,1] & class_pt[,2] == sample_pt[j,1]
#       neg_idx = class_pt[,2] == sample_pt[i,1] & class_pt[,1] == sample_pt[j,1]
#       d2lp_pos = -n_p[pos_idx]^2 - Z[pos_idx] * n_p[pos_idx]
#       d2lp_neg = -n_p[neg_idx]^2 - Z[neg_idx] * n_p[neg_idx]
#       d2lp[i,j] = sum(d2lp_pos) - sum(d2lp_neg)
#     }
#   }
  
  d2lp = matrix(0, length(mu), length(mu)) # Hessian of log likelihood
  for (i in 1:nrow(sample_pt)) {
    for (j in 1:nrow(sample_pt)) {
      ## is j preferred over i?
      s_ij_row = class_pt[,1] == sample_pt[i,1] & class_pt[,2] == sample_pt[j,1]
      s_ji_row = class_pt[,2] == sample_pt[i,1] & class_pt[,1] == sample_pt[j,1]
      
      # preferred: j 2nd and positive or j 1st and positive
      pos_idx = (s_ij_row & class_pt[,3] == 1) |
                (s_ji_row & class_pt[,3] == -1)
      # not preferred: j 2nd and negative or j 1st and positive
      neg_idx = (s_ij_row & class_pt[,3] == -1) |
                (s_ji_row & class_pt[,3] == 1)
      
#       pos_idx = class_pt[,1] == sample_pt[i,1] & class_pt[,2] == sample_pt[j,1]
#       neg_idx = class_pt[,2] == sample_pt[i,1] & class_pt[,1] == sample_pt[j,1]
      d2lp_pos = -n_p[pos_idx]^2 - Z[pos_idx] * n_p[pos_idx]
      d2lp_neg = -n_p[neg_idx]^2 - Z[neg_idx] * n_p[neg_idx]
      d2lp[i,j] = sum(d2lp_pos) - sum(d2lp_neg)
    }
  }
  
  ymu = 2*p - 1 # mean
  ys2 = 4 * p * (1-p) # covariance
  
  return(list(p=p, lp=lp, dlp=dlp, d2lp=d2lp, ymu=ymu, ys2=ys2))
}


# NOTE: meanFn is ignored
## Laplacian estimate of posterior mode from sample estimation
##  uses Newton's method with line search to optimize evidence
#     sample_pt   - features for objects compared
#     class_pt    - definition of pairs of objects to be compared; indexes into sample_pt starting at 1
#     meanFn      - mean function for samples
#     kernelFn    - covariance function for samples
#     sigma_n     - noise in judgements of sample preferences
#     ...         - other parameters to pass to kernelFn or meanFn
infPrefLaplace = function(sample_pt, class_pt, meanFn, kernelFn, sigma_n, tol=1e-6, max_iter=100, optmethod='me', ...) {
  
#   m = mean.const(sample_pt, 0) # TODO: replace with other
  sample_val = sample_pt[,-1]
  m = mean.const(sample_val, 0) # TODO: replace with other
#   K = kernelFn(sample_pt, sample_pt, deriv=FALSE, ...)
  K = kernelFn(sample_val, sample_val, deriv=FALSE, ...)
  liks = likPref(sample_pt, class_pt, m, K, sigma_noise=sigma_n)
  
  f = m
  
  Psi_old = Inf
  Psi_new = -sum(liks$lp)
  
  a = rep(0, length(f))
  pstep = list(liks=liks)
  iter = 0
  while (Psi_old - Psi_new > tol & iter < max_iter) {
    print(paste(round(Psi_old,3), round(Psi_new,3), Psi_old-Psi_new))
    
    Psi_old = Psi_new
    
    W = -pstep$liks$d2lp # W = -d2lp
    W[W<0] = 0
    
    sW = sqrt(W)                                      # W^1/2
    L = chol(diag(1, ncol(K)) + sW %*% t(sW) * K)     # L = cholesky(I + sW K sW)
    b = W %*% f + pstep$liks$dlp                      # b = W f + dlp
    da = b - sW %*% solve(L, sW %*% (K %*% b)) - a    # a = b - sW L' \ (L \ (sW K b))
    
    # compute best step size
    # then next point to test
    if (optmethod=='me') {
      step_best = findMinBisect(prefStepCurry, xmin=0.1, xmax=2, tol=1e-10, da, a, liks, f, K, sample_pt, class_pt, sigma_n)
      pstep = prefStepCurry(step_best$xbest, da, a, liks, f, K, sample_pt, class_pt, sigma_n) # version w/my optim code
      a = step_best$xbest*da + a      
    } else {
      step_best = optimx(c(0.5), fn=prefStepOptimx, gr=NULL, hess=NULL, lower=0.1, upper=2, method=optmethod, itnmax=NULL, hessian=FALSE, control=NULL, da,a,liks,f,K,sample_pt,class_pt,sigma_n)
      pstep = prefStepCurry(as.numeric(step_best$par), da, a, liks, f, K, sample_pt, class_pt, sigma_n)
      a = as.numeric(step_best$par)*da + a
    }

    f = pstep$f
    
    Psi_new = pstep$Psi
    iter = iter+1
  }
  return(list(p_map=pstep, f_map=f, K=K, Psi=Psi_new))
}


## model evidence
#   lp    - log probability of model
#   f     - latent feature values
#   alpha - aka "a" from model, see Rasmussen + Williams p.46
prefPsi = function(lp, f, alpha) {
  #Psi = -sum(lp) + 1/2 * t(f) %*% K^-1 %*% f
  lp = lp[lp>-Inf & lp<Inf]
  Psi = -sum(lp) + 0.5 * t(alpha) %*% f
  return(as.numeric(Psi))
}

## take a step of magnitude "step" in "a" along "da".
## then compute score of likelihood of this new model
#   aka line search scoring
prefStep = function(step, da, a, liks, f, K, sample_pt, class_pt, sigma_noise) {
  a = step*da + a
  f = K %*% a
  liks = likPref(sample_pt, class_pt, f, K, sigma_noise)
  badidx = which(liks$lp==Inf | liks$lp==-Inf) # HACK: remove anything infinite
  Psi = prefPsi(liks$lp[-badidx], f, a)
  return(list(Psi=Psi, liks=liks, f=f))
}

## change output for optimization procedures
prefStepOptimx = function(step, da, a, liks, f, K, sample_pt, class_pt, sigma_noise) {
  a = step*da + a
  f = K %*% a
  liks = likPref(sample_pt, class_pt, f, K, sigma_noise)
  badidx = which(liks$lp==Inf | liks$lp==-Inf) # HACK: remove anything infinite
  Psi = prefPsi(liks$lp[-badidx], f, a)
  return(as.numeric(Psi))
}

prefStepCurry = function(step, ...) {
  prefStep(step, ...)
}


infPrefLaplaceCurry = function(kernel_params) {
  load('optim_pref_data.RData')
  nparam = length(kernel_params)
  
  ## take in list of parameters and produce optimized score
  score = infPrefLaplace(params$sample_pt, params$class_pt, 
                         params$meanFn, params$kernelFn, 
                         params$sigma_noise, params$tol, params$max_iter, 
                         length_scale=kernel_params[1:(nparam-1)],
                         variance_scale=kernel_params[nparam])
  
#   score = predictGP(params$x, params$y, params$x_test, params$sigma.noise, params$k.x_x, params$meanFn, params$kernelFn, length_scale=kernel_scale[1:(nparam-1)], variance_scale=kernel_scale[nparam])
  
  return(score$Psi)
}

predictGP.score.curry = function(kernel_scale) {
  load('./optim_data.RData')
  nparam = length(kernel_scale)
  score = predictGP(params$x, params$y, params$x_test, params$sigma.noise, params$k.x_x, params$meanFn, params$kernelFn, length_scale=kernel_scale[1:(nparam-1)], variance_scale=kernel_scale[nparam])
  return(-score$lZ)
}


## find minimum value of a convex function between two input values.
#   fn - should return it's value as first parameter
#   xmin, xmax - two edges of range to check
findMinBisect = function(fn, xmin, xmax, tol=1e-10, ...) {
  fmin = fn(xmin, ...)
  fmax = fn(xmax, ...)
  
  # if fmax is Inf, pull xmax closer to xmin
  #   issue is overshooting and missing optimum, so take small step, say 1/10 of range differece
  #   may need to diagnose why fmax is returning infinite score
  vinf = TRUE
  while (vinf == TRUE) {
    xmax = xmin + (xmax - xmin) * 0.9
    fmax = fn(xmax, ...)
    vinf = fmax[[1]] == Inf
  }
  
  # compute midpoint x its value
  xcur = (xmax - xmin)/2
  fcur = fn(xcur, ...)
  
  #   cat("min: ", fmin[[1]], '\n')
  #   cat("max: ", fmax[[1]], '\n')
  #   cat("cur: ", fcur[[1]], '\n')
  
  p1 = c(xmin, fmin[[1]])
  p2 = c(xmax, fmax[[1]])
  p3 = c(xcur, fcur[[1]])
  pts = rbind(p1,p2,p3)
  
  old_vmax = Inf
  vmax = pts[which.max(pts[,2]),2]
  
  while (old_vmax - vmax > tol) {
    old_vmax = vmax
    
    max_idx = which.max(pts[,2])
    pts = pts[-max_idx,] # remove max value
    xmid = sum(pts[,1])/2
    fmid = fn(xmid, ...)
    pts = rbind(pts, c(xmid, fmid[[1]]))
    
    max_idx = which.max(pts[,2])
    vmax = pts[max_idx,2]
    #     cat('next: ', vmax, '\n')
  }
  min_idx = which.min(pts[,2])
  xbest = pts[min_idx,1]
  fbest = pts[min_idx,2]
  return(list(xbest=xbest, fbest=fbest))
}


## optimize inference function results by setting hyperparameters using grid search
#     lengthscale_grid  - matrix (D dimensions, where D is dimensionality of samples) of kernel length scales to test
#     sigma_grid        - vector of preference function noise values to test
#     sample_pt   - features for objects compared
#     class_pt    - definition of pairs of objects to be compared; indexes into sample_pt starting at 1
#     infFn       - inference function to evaluate posterior
#     meanFn      - mean function for samples
#     kernelFn    - covariance function for samples
optimizeHyper = function(hypmethod='me', optmethod='me', lengthscale_grid, sigma_grid, sample_pt, class_pt, infFn, meanFn, kernelFn) {
  
  if (hypmethod=='me') {
    # search hyperparameter settings
    grid_points = list()
    for (sigman in sigma_grid) {
      for (lidx in 1:nrow(lengthscale_grid)) {
        lenscale = lengthscale_grid[lidx,]
        tpoint = infFn(sample_pt=sample_pt, class_pt=class_pt, meanFn=meanFn, kernelFn=kernelFn, sigma_n=sigman, tol=1e-8, max_iter=10, optmethod=optmethod, lenscale)
        grid_points[[paste(c(lenscale, sigman), collapse=' ')]] = tpoint
      }
    }
    
    # find highest scoring set of hyperparameters and corresponding results
    psi_grid = lapply(grid_points, function(x) x$p_map$Psi)
    psi_grid[which.min(unlist(psi_grid))] # minimum score (best)
    tbest = grid_points[[which.min(unlist(psi_grid))]] # results with best hyperparameters
    
    minparam = names(grid_points)[which.min(unlist(psi_grid))]
    minparam = as.numeric(unlist(strsplit(minparam, ' ')))
    nparam = length(minparam)
    lenscale = minparam[1:(nparam-1)]
    sigma_n = minparam[nparam]
    
    return(list(model=tbest, sigma_n=sigma_n, lenscale=lenscale, f_map=tbest$f_map, K=tbest$K, W=tbest$p_map$liks$d2lp))
  }
  else {
    len_range = apply(lengthscale_grid, 2, range)
    len_min = apply(len_range, 2, min)
    len_max = apply(len_range, 2, max)
    len_mid = apply(len_range, 2, mean)
    
    sigma_range = range(sigma_grid)
    
    outres = optimx(c(len_mid,mean(sigma_range)), 
                    fn=infPrefLaplaceOptimx, gr=NULL, hess=NULL, 
                    lower=c(len_min, min(sigma_range)), 
                    upper=c(len_max, max(sigma_range)), 
                    method=hypmethod, 
                    itnmax=NULL, hessian=FALSE, control=NULL, 
                    sample_pt, class_pt, meanFn, kernelFn, optmethod=optmethod)
    optparam = unlist(outres$par)
    nparam = length(optparam)
    optlen = optparam[1:(nparam-1)]
    optsigma = optparam[nparam]
    tbest = infPrefLaplace(sample_pt, class_pt, meanFn, kernelFn, tol=1e-06, max_iter=100, sigma_n=optsigma, optmethod=optmethod, optlen)
    
    return(list(model=tbest, sigma_n=optsigma, lenscale=optlen, f_map=tbest$f_map, K=tbest$K, W=tbest$p_map$liks$d2lp))
    
#     outres = optimx(c(0.0025,0.0025), fn=infPrefLaplaceOptimx, gr=NULL, hess=NULL, lower=c(0.0001,0.0015), upper=c(0.005,0.0050), method=hypmethod, itnmax=NULL, hessian=FALSE, control=NULL, x_sample, x_class, mean.const, kernel.SqExpND, optmethod=optmethod)
  }
  
}

infPrefLaplaceOptimx = function(inparam, sample_pt, class_pt, meanFn, kernelFn, tol=1e-6, max_iter=100, optmethod='me', ...) {
  nparam = length(inparam)
  lengthscale = inparam[1:(nparam-1)]
  sigma_n = inparam[nparam]
  outlist = infPrefLaplace(sample_pt=sample_pt, class_pt=class_pt, meanFn=meanFn, kernelFn=kernelFn, sigma_n=sigma_n, tol=tol, max_iter=max_iter, optmethod=optmethod, lengthscale)
  return(as.numeric(outlist$Psi))
}


## predict preferences between pairs of samples using model provided
#     t_pairs     - pairs of test objects to be compared; indexes into sample_pt starting at 1
#     sample_pt   - features for objects compared
#     f           - latent feature values
#     W           - minus Hessian of log likelihood
#     K           - training data sample covariance
#     kernelFn    - covariance function for samples
#     sigma_n     - noise in judgements of sample preferences
#     ...         - additional parameters for kernel function
prefPredict = function(model, t_pairs, sample_pt, f, W, K, sigma_n, kernelFn, ...) {
  
  # covariance b/t test samples and training samples
  # kt = [K(r,x1) ... K(r,xn) ; K(s,x1) ... K(s,xn)]'
  t_pt1 = as.matrix(sample_pt[t_pairs[,1],], ncol=ncol(sample_pt))
  t_pt2 = as.matrix(sample_pt[t_pairs[,2],], ncol=ncol(sample_pt))

  kt = t(rbind(kernelFn(t_pt1, sample_pt, deriv=FALSE, ...), 
               kernelFn(t_pt2, sample_pt, deriv=FALSE, ...)))
  
  # test point covariance matrix from block form
  # Kt = [ Krr Krs ; Ksr Kss]
  ntest = nrow(t_pairs)
  Krr = kernelFn(t_pt1, t_pt2, deriv=FALSE, ...)
  Kss = kernelFn(t_pt2, t_pt2, deriv=FALSE, ...)
  Krs = kernelFn(t_pt1, t_pt2, deriv=FALSE, ...)
  Ksr = kernelFn(t_pt2, t_pt1, deriv=FALSE, ...)
  Kt = rbind(cbind(Krr, Krs), cbind(Ksr, Kss))
  
  
  W[W<0] = 0
  sW = sqrt(W)
  L = chol(diag(1, ncol(K)) + sW %*% t(sW) * K)     # L = cholesky(I + sW K sW)
  
  
  # predictive means
  # mu_t = t(kt) %*% a # t(kt) alpha
  mu_t = t(kt) %*% solve(t(L), solve(L)) %*% f 
  # mu_t = t(kt) %*% K^-1 %*% f  # this may be unstable, need to check
  mu_t = matrix(mu_t, ncol=2)
  
  
  
  ## predictive variances is broken
  # may be due to repeat of identical samples being summed?
  # cf GP regression model
  
  # predictive variances using methods from Rasmussen + Williams
  
   ## v = L \ (sW kt)
#   v = solve(L, sW %*% kt)
   ## V = Kt - v' v
#   Kstar = Kt + sqrt(t(v) %*% v)
  
#   apply(kt * (L %*% kt), 2, sum)
  # kss + sum(Ks .* (L * Ks), 1)'
  
  v = Kt + apply(kt * L %*% kt, 2, sum) # maybe? Kt is 10x10 while second factor is 1x10
  Kstar = Kt + sqrt(t(v) %*% v)
  sigma_s = 2*sigma_n + Kstar[1:ntest,1:ntest] + Kstar[(ntest+1):(2*ntest),(ntest+1):(2*ntest)] - Kstar[1:ntest,(ntest+1):(2*ntest)] - Kstar[(ntest+1):(2*ntest),1:ntest]
  
  # predict preference using predictive means + variances
  prefPred = prefFnVec(mu_t, t_pairs, sigma_s)
  #   prefPred = prefFn(mu_t, sigma_n)
  
  return(list(pred=prefPred, mu_s=mu_t, sigma_s=sigma_s))
}

## predict preferences between pairs of samples using model provided
#     t_pairs     - pairs of test objects to be compared; indexes into sample_pt starting at 1
#     sample_pt   - features for objects compared
#     f           - latent feature values
#     W           - minus Hessian of log likelihood
#     K           - training data sample covariance
#     kernelFn    - covariance function for samples
#     sigma_n     - noise in judgements of sample preferences
#     ...         - additional parameters for kernel function
prefPredict.v2 = function(model, t_pair, t_sample, sample_pt, f, W, K, sigma_n, kernelFn, ...) {
  
  # covariance b/t test samples and training samples
  # kt = [K(r,x1) ... K(r,xn) ; K(s,x1) ... K(s,xn)]'
  t_pt1 = as.matrix(t_sample[t_pair[,1],])
  t_pt2 = as.matrix(t_sample[t_pair[,2],])
  
  s_val = sample_pt[,-1]
  t1_val = t_pt1[,-1]
  t2_val = t_pt2[,-1]
  kt = t(rbind(kernelFn(t1_val, s_val, deriv=FALSE, ...), 
               kernelFn(t2_val, s_val, deriv=FALSE, ...)))
  
#   kt = t(rbind(kernelFn(t_pt1, sample_pt, deriv=FALSE, ...), 
#                kernelFn(t_pt2, sample_pt, deriv=FALSE, ...)))
  
  # test point covariance matrix from block form
  # Kt = [ Krr Krs ; Ksr Kss]
  ntest = nrow(t_pair)
  Krr = kernelFn(t1_val, t2_val, deriv=FALSE, ...)
  Kss = kernelFn(t2_val, t2_val, deriv=FALSE, ...)
  Krs = kernelFn(t1_val, t2_val, deriv=FALSE, ...)
  Ksr = kernelFn(t2_val, t1_val, deriv=FALSE, ...)
  
#   Krr = kernelFn(t_pt1, t_pt2, deriv=FALSE, ...)
#   Kss = kernelFn(t_pt2, t_pt2, deriv=FALSE, ...)
#   Krs = kernelFn(t_pt1, t_pt2, deriv=FALSE, ...)
#   Ksr = kernelFn(t_pt2, t_pt1, deriv=FALSE, ...)
  Kt = rbind(cbind(Krr, Krs), cbind(Ksr, Kss))
  
  
  W[W<0] = 0
  sW = sqrt(W)
  L = chol(diag(1, ncol(K)) + sW %*% t(sW) * K)     # L = cholesky(I + sW K sW)
  
  
  # predictive means
  # mu_t = t(kt) %*% a # t(kt) alpha
  mu_t = t(kt) %*% solve(t(L), solve(L)) %*% f 
  # mu_t = t(kt) %*% K^-1 %*% f  # this may be unstable, need to check
  mu_t = matrix(mu_t, ncol=2)
  
  
  
  ## predictive variances is broken
  # may be due to repeat of identical samples being summed?
  # cf GP regression model
  
  # predictive variances using methods from Rasmussen + Williams
  
  ## v = L \ (sW kt)
  #   v = solve(L, sW %*% kt)
  ## V = Kt - v' v
  #   Kstar = Kt + sqrt(t(v) %*% v)
  
  #   apply(kt * (L %*% kt), 2, sum)
  # kss + sum(Ks .* (L * Ks), 1)'
  
  v = Kt + apply(kt * L %*% kt, 2, sum) # maybe? Kt is 10x10 while second factor is 1x10
  Kstar = Kt + sqrt(t(v) %*% v)
  sigma_s = 2*sigma_n + Kstar[1:ntest,1:ntest] + Kstar[(ntest+1):(2*ntest),(ntest+1):(2*ntest)] - Kstar[1:ntest,(ntest+1):(2*ntest)] - Kstar[(ntest+1):(2*ntest),1:ntest]
  
  rownames(sigma_s) = gsub('\\.(\\d+)*', '', rownames(sigma_s))
  colnames(sigma_s) = gsub('\\.(\\d+)*', '', colnames(sigma_s))
  
  latent = data.frame(label=seq(1:nrow(mu_t)), f=mu_t[,2], s2=diag(sigma_s))
  ggplot(latent, aes(label, f)) + geom_point() + geom_errorbar(aes(ymin=f-sqrt(s2), ymax=f+sqrt(s2)), colour='grey80') + theme_bw()
  
#   sigma = data.frame(p1=rownames(sigma_n), p2=colnames(sigma_n), sigma_n)
  
  
  # predict preference using predictive means + variances
  prefPred = prefFnVec(mu_t, t_pair, sigma_s)
  pref = data.frame(label=seq(1:nrow(t_pair)), pref=prefPred, s2=diag(sigma_s))
  ggplot(pref, aes(label, pref)) + geom_point() #+ geom_errorbar(aes(ymin=pref-sqrt(s2), ymax=pref+sqrt(s2)), colour='grey80') + theme_bw()
#   prefPred = prefFn(mu_t, sigma_n)
  
  return(list(pred=prefPred, mu_s=mu_t, sigma_s=sigma_s))
}


#### ACTIVE LEARNING ####


## point that maximizes the expected information gain
#   f_cur       - current function values
#   f_test      - set of test point function values
#   x_test      - set of test point locations
#   sigma_n     - standard deviation of measurement noise
#   slack       - control for explore-exploit tradeoff in selecting points
al.maxExpectedImprovement = function(f_cur, f_test, f_cov, x_test, sigma_n, slack=0.01, iter=1) {
  f_plus = max(f_cur) # current best point
  
#   usr_sample = arrange(usr_sample, s_wave)
#   last_pt = usr_sample[nrow(usr_sample), c('label')] # sample value most recently tested; use for pairwise tests
  
#   z_t = (f_test - f_plus - slack) / sigma_n
  z_t = (f_test - f_plus - slack)/diag(f_cov)
#   ei = (f_test - f_plus - slack) * pnorm(z_t) + sigma_n * dnorm(z_t)
  ei = (f_test - f_plus - slack) * pnorm(z_t) + diag(f_cov) * dnorm(z_t)
  colnames(ei) = 'ei'
  
  ## get next set of possible sample points sorted by expected information
  tmp = cbind(ei, x_test)
  top10 = tmp[order(-tmp[,1]),]
  next_sample10 = top10[1:10,-c(1)]
  
  
#   next_pt = which.max(ei)
  #   next_pt = t_pairs[next_pt,]
  #   next_sample = next_pt[next_pt!=last_pt]
#   next_sample = x_test[next_pt,]
  next_sample = next_sample10[1,]
  
  png(paste('ei_', iter, '.png', sep=''))
  print(
    ggplot(top10, aes(Var1, Var2, z=ei)) + stat_contour(geom='polygon', aes(fill=..level..), bins=15)  + geom_point(data=next_sample10, aes(x=Var1, y=Var2, z=1), size=5, colour='orange') + theme_bw()
  )
  dev.off()
  
  return(next_sample)
}

al.maxExpectedImprovement.v2 = function(f_cur, f_test, f_cov, x_test, slack=0.01, iter=1) {
  f_plus = max(f_cur) # current best point
  
  #   usr_sample = arrange(usr_sample, s_wave)
  #   last_pt = usr_sample[nrow(usr_sample), c('label')] # sample value most recently tested; use for pairwise tests
  
  #   z_t = (f_test - f_plus - slack) / sigma_n
  z_t = (f_test - f_plus - slack)/f_cov
  #   ei = (f_test - f_plus - slack) * pnorm(z_t) + sigma_n * dnorm(z_t)
  ei = (f_test - f_plus - slack) * pnorm(z_t) + f_cov * dnorm(z_t)
#   ei = as.matrix(ei, ncol=1)
#   colnames(ei) = 'ei'
  
  pred = data.frame(label=seq(1:length(f_test)), f=f_test, s2=f_cov, ei=ei)
  names(pred) = c('label', 'f', 's2', 'ei')
  png(paste('ei_', iter, '.png', sep=''))
  print(
    ggplot(pred, aes(label, f)) + geom_point() + theme_bw() + geom_errorbar(aes(ymin=f-sqrt(s2), ymax=f+sqrt(s2)), colour='grey80') + geom_point(aes(label, ei), colour='orange')
  )
  dev.off()
  
  ## get next set of possible sample points sorted by expected information
  tmp = cbind(ei, x_test)
  next_sample = arrange(tmp, desc(ei))[1,-1]
#   top10 = tmp[order(-tmp[,1]),]
#   next_sample10 = top10[1:min(10,nrow(top10)),-c(1)]
  
  
  #   next_pt = which.max(ei)
  #   next_pt = t_pairs[next_pt,]
  #   next_sample = next_pt[next_pt!=last_pt]
  #   next_sample = x_test[next_pt,]
#   next_sample = next_sample10[1,]
  
#   names(top10) = c('ei', paste('Var', seq(1:(ncol(top10)-1)), sep=''))
  
#   png(paste('ei_', iter, '.png', sep=''))
#   print(
#     ggplot(top10, aes(Var1, Var2, z=ei)) + stat_contour(geom='polygon', aes(fill=..level..), bins=15)  + geom_point(data=next_sample10, aes(x=Var1, y=Var2, z=1), size=5, colour='orange') + theme_bw()
#   )
#   dev.off()
  
  return(next_sample)
}

## expands out a parameter range into a sequence of n points between the range bounds
paramGrid = function(npts, paramMin, paramMax) {
  return(seq(paramMin, paramMax, by=(paramMax-paramMin)/npts))
}


## expands a set of learning parameter ranges into a npts X npts grid of points to search
testGrid = function(learn_params) {
  tpts = list()
  for (i in 1:nrow(learn_params)) {
    tpts[[as.character(learn_params$param[i])]] = paramGrid(learn_params$npts[i], learn_params$min[i], learn_params$max[i])
  }
  tpts = expand.grid(tpts)
  return(tpts)
}

## uniformly generate random points b/t min and max
paramRandom = function(npts, paramMin, paramMax) {
  return(runif(n=npts, min=paramMin, max=paramMax))
}

## generate npts random sample points w/in ranges specified by learn_params
randomPts = function(npts, learn_params) {
  rpts = list()
  for (i in 1:nrow(learn_params)) {
    rpts[[as.character(learn_params$param[i])]] = paramRandom(npts, learn_params$min[i], learn_params$max[i])
  }
  rpts = expand.grid(rpts)
  return(rpts)
}