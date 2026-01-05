library(dplyr)
library(ggplot2)   
library(tidyr)


read_norm_metrics <- function(path,filter) {
  mycolname <- c("Prec@5","Prec@10","MRR","MAP","kernel","time","seed","mode")
  df <- read.csv(path, header = FALSE)
  colnames(df) <- mycolname
  df$filter<-filter
  return(df[df$mode == "normalized", ])
  }
#No filter
ptk_no_filter  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_PTK_no_filter.csv","no_filter")
stk_no_filter  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_STK_no_filter.csv","no_filter")
sstk_no_filter <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_SSTK_no_filter.csv","no_filter")

# For each kernel compute mean
cols <- c("Prec@5", "Prec@10", "MRR", "MAP")
round(sapply(cols, function(col) mean(ptk_no_filter[[col]])),2)
round(sapply(cols, function(col) mean(stk_no_filter[[col]])),2)
round(sapply(cols, function(col) mean(sstk_no_filter[[col]])),2)

#PTK   &  0.68 & 0.62 & 0.82 & 0.20 & 11913 \\ #no filter
#STK   &  0.71 & 0.67 & 0.85 & 0.26 & 11315 \\ #no filter 
#SSTK  &  0.64 & 0.60 & 0.78 & 0.20 & 11789 \\ #no filter   
#TF-IDF& 0.66 & 0.61 & 0.79 & 0.35 & 165    \\final
#CodeBERT& 0.49  & 0.46 & 0.50 & 0.18 & 0.09  


no_filter_all <- bind_rows(ptk_no_filter, stk_no_filter, sstk_no_filter) 

#Topk
ptk_topk  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_PTK_topk.csv","topk")
stk_topk  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_STK_topk.csv","topk")
sstk_topk <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_SSTK_topk.csv","topk")

# For each kernel compute mean
round(sapply(cols, function(col) mean(ptk_topk[[col]])),2)
round(sapply(cols, function(col) mean(stk_topk[[col]])),2)
round(sapply(cols, function(col) mean(sstk_topk[[col]])),2)



#PTK     & 0.68  & 0.62 & 0.82 & 0.33 & 11913.50 \\ #topk
#STK     & 0.71  & 0.67 & 0.85 & 0.38 & 11315.10 \\ #topk
#SSTK    & 0.64  & 0.60 & 0.78 & 0.33 & 11789.30 \\ #topk
#TF-IDF  & 0.66  & 0.61 & 0.79 & 0.35             \\final
#CodeBERT& 0.49  & 0.46 & 0.50 & 0.18 &  0.09 


topk_all <- bind_rows(ptk_topk, stk_topk, sstk_topk) 

#threshold
ptk_threshold  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_PTK_threshold.csv","threshold")
stk_threshold  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_STK_threshold.csv","threshold")
sstk_threshold <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_SSTK_threshold.csv","threshold")

# For each kernel compute mean
round(sapply(cols, function(col) mean(ptk_threshold[[col]])),2)
round(sapply(cols, function(col) mean(stk_threshold[[col]])),2)
round(sapply(cols, function(col) mean(sstk_threshold[[col]])),2)
#PTK   & 0.68  & 0.62  &  0.82  &  0.20 & 11913.50 \\ #threshold
#STK   & 0.71  & 0.67  &  0.85  &  0.25 & 11315.10 \\ #threshold
#SSTK  & 0.49  & 0.44  &  0.62  &  0.10 & 11789.30 \\ #threshold This changed the most when prec denom was set to K and mrr condition was corrected
#TF-IDF  & 0.66  & 0.61 & 0.79 & 0.35             \\final
#CodeBERT& 0.49  & 0.46 & 0.50 & 0.18 &  0.09


threshold_all <- bind_rows(ptk_threshold, stk_threshold, sstk_threshold) 

#topk_threshold
ptk_topk_threshold  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_PTK_topk_threshold.csv","topk_threshold")
stk_topk_threshold  <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_STK_topk_threshold.csv","topk_threshold")
sstk_topk_threshold <- read_norm_metrics("~/treekernel-jss2025/results_final/RQ1/metrics_RQ1_SSTK_topk_threshold.csv","topk_threshold")

# For each kernel compute mean
round(sapply(cols, function(col) mean(ptk_topk_threshold[[col]])),2)
round(sapply(cols, function(col) mean(stk_topk_threshold[[col]])),2)
round(sapply(cols, function(col) mean(sstk_topk_threshold[[col]])),2)
#PTK   & 0.68  & 0.62  &  0.82  &  0.33 & 11913.50 \\ #topk_threshold
#STK   & 0.71  & 0.67  &  0.85  &  0.38 & 11315.10 \\ #topk_threshold
#SSTK  & 0.49  & 0.44  &  0.62  &  0.24 & 11789.30 \\ #topk_threshold
#TF-IDF  & 0.66  & 0.61 & 0.79 & 0.35              \\final
#CodeBERT& 0.49  & 0.46 & 0.50 & 0.18 &  0.09


topk_threshold_all <- bind_rows(ptk_topk_threshold, stk_topk_threshold, sstk_topk_threshold) 

