mycolname<-c("Prec@5","Prec@10","MRR","MAP","kernel","time","seed","mode","numdocs")

load_hybrid <- function(file_path, kernel_name) {
  df <- read.csv(file_path, header = TRUE)
  colnames(df) <- mycolname
  df$kernel[df$kernel == kernel_name] <- paste0( kernel_name,"-Hybrid")
  df2 <- df[df$numdocs == "100", ]
  df_norm <- df2[df2$mode == "normalized", ]
  df_final <- df_norm %>% select( -numdocs)
  return(df_final)
}
# Load 'No filter' results
hybrid_ptk_norm  <- load_hybrid("~/treekernel-jss2025/results_final//RQ4_v3/metrics_RQ4_hybrid_PTK_no_filter.csv",  "PTK")
hybrid_stk_norm  <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_STK_no_filter.csv",  "STK")
hybrid_sstk_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_SSTK_no_filter.csv", "SSTK")


cols <- c("Prec@5", "Prec@10", "MRR", "MAP")

# For each kernel compute mean
round(sapply(cols, function(col) mean(hybrid_ptk_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_stk_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_sstk_norm[[col]])),2)

#Prec@5 Prec@10     MRR     MAP 
#PTK hybrid  0.70    0.65    0.84    0.09 
#STK hybrid  0.72    0.67    0.85    0.09 
#SSTK hybrid 0.70    0.66    0.82    0.09 

# Load 'topk filter' results
hybrid_ptk_topk_norm <- load_hybrid("~/treekernel-jss2025/results_final//RQ4_v3/metrics_RQ4_hybrid_PTK_topk.csv","PTK")
hybrid_stk_topk_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_STK_topk.csv","STK")
hybrid_sstk_topk_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_SSTK_topk.csv","SSTK")

round(sapply(cols, function(col) mean(hybrid_ptk_topk_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_stk_topk_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_sstk_topk_norm[[col]])),2)
mean(hybrid_sstk_topk_norm$time)
#Prec@5   Prec@10     MRR     MAP 
#PTK hybrid  0.70    0.65    0.84    0.37 
#STK hybrid  0.72    0.67    0.85    0.37 
#SSTK hybrid 0.70    0.66    0.82    0.37 

# Load 'topk threshold filter' results
hybrid_ptk_topk_threshold_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_PTK_topk_threshold.csv","PTK")
hybrid_stk_topk_threshold_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_STK_topk_threshold.csv","STK")
hybrid_sstk_topk_threshold_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_SSTK_topk_threshold.csv","SSTK")

round(sapply(cols, function(col) mean(hybrid_ptk_topk_threshold_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_stk_topk_threshold_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_sstk_topk_threshold_norm[[col]])),2)

#PTK hybrid  &  0.70  &  0.65 &   0.84  &  0.37
#STK hybrid  &  0.72  &  0.67 &   0.85  &  0.36 
#SSTK hybrid &  0.46  &  0.41 &   0.59  &  0.16 


# Load 'threshold filter' results
hybrid_ptk_threshold_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_PTK_threshold.csv","PTK")
hybrid_stk_threshold_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_STK_threshold.csv","STK")
hybrid_sstk_threshold_norm <- load_hybrid("~/treekernel-jss2025/results_final/RQ4_v3/metrics_RQ4_hybrid_SSTK_threshold.csv","SSTK")

round(sapply(cols, function(col) mean(hybrid_ptk_threshold_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_stk_threshold_norm[[col]])),2)
round(sapply(cols, function(col) mean(hybrid_sstk_threshold_norm[[col]])),2)
#PTK hybrid  & 0.70    0.65    0.84    0.09 
#STK hybrid  & 0.72    0.67    0.85    0.09 
#SSTk hybrid & 0.46    0.41    0.59    0.04