# RQ3 LOC 

library(dplyr)
library(ggplot2)   
library(tidyr)
mycolname<-c("Prec@5", "Prec@10", "MRR", "MAP", "kernel",  "time", "seed", "mode")

load_size_metrics <- function(path, size_label, mycolname) {
  df <- read.csv(path, header = FALSE)
  colnames(df) <- mycolname
  df$size <- size_label
  df <- df[df$mode == "Normalized", ]   # keep only normalized
  df <- select(df,-mode)
  return(df)
}

# Small (< 6 LOC)
ptk_lessThan6LOC <- load_size_metrics(
  "~/treekernel-jss2025/results_final/RQ3/Small/metrics_RQ3_Small_PTK_topk.csv",
  "< 6", mycolname
)

sstk_lessThan6LOC <- load_size_metrics(
  "~/treekernel-jss2025/results_final/RQ3/Small/metrics_RQ3_Small_SSTK_topk.csv",
  "< 6", mycolname
)

stk_lessThan6LOC <- load_size_metrics(
  "~/treekernel-jss2025/results_final/RQ3/Small/metrics_RQ3_Small_STK_topk.csv",
  "< 6", mycolname
)

# Large (> 10 LOC)
ptk_moreThan10LOC <- load_size_metrics(
  "~/treekernel-jss2025/results_final/RQ3/Large/metrics_RQ3_Large_PTK_topk.csv",
  "> 10", mycolname
)

sstk_moreThan10LOC <- load_size_metrics(
  "~/treekernel-jss2025/results_final/RQ3/Large/metrics_RQ3_Large_SSTK_topk.csv",
  "> 10", mycolname
)

stk_moreThan10LOC <- load_size_metrics(
  "~/treekernel-jss2025/results_final/RQ3/Large/metrics_RQ3_Large_STK_topk.csv",
  "> 10", mycolname
)

cols <- c("Prec@5", "Prec@10", "MRR", "MAP")
round(sapply(cols, function(col) mean(stk_lessThan6LOC[[col]])), 2)
round(sapply(cols, function(col) mean(stk_moreThan10LOC[[col]])), 2)

round(sapply(cols, function(col) mean(ptk_lessThan6LOC[[col]])), 2)
round(sapply(cols, function(col) mean(ptk_moreThan10LOC[[col]])), 2)

round(sapply(cols, function(col) mean(sstk_lessThan6LOC[[col]])), 2)
round(sapply(cols, function(col) mean(sstk_moreThan10LOC[[col]])), 2)

#Table 3 new
# STK  Small  0.87    0.86    0.94    0.69 
# PTK  Small  0.89    0.87    0.94    0.70 
# SSTK Small  0.88    0.86    0.94    0.70 

# STK  Large  0.67    0.62    0.83    0.32
# PTK  Large  0.62    0.56    0.79    0.25 
# SSTK Large  0.58    0.53    0.74    0.25
             

mycolname<-c("Prec@5", "Prec@10", "MRR", "MAP", "kernel",  "time", "seed", "mode")
tfidf<-read.csv("~/treekernel-jss2025/results_final/RQ3/metrics_RQ3_TFIDF.csv",header = FALSE)
colnames(tfidf) <- mycolname
tfidf_small<- tfidf[tfidf$mode == "Small", ] 
tfidf_large<- tfidf[tfidf$mode == "Large", ] 
tfidf_small$size<-"< 6"
tfidf_large$size<-"> 10"

cols <- c("Prec@5", "Prec@10", "MRR", "MAP")
round(sapply(cols, function(col) mean(tfidf_small[[col]])), 2)
round(sapply(cols, function(col) mean(tfidf_large[[col]])), 2)
#TFIDF small  0.75    0.74    0.84    0.59 
#TFIDF large  0.67    0.62    0.83    0.35 

codebert<-read.csv("~/treekernel-jss2025/results/codebert-metrics.csv")[,1:6]
codebert <- codebert[, -5] #exclude MAP@all
colnames(codebert) <- c("Prec@5", "Prec@10", "MRR", "MAP","experiment")
codebert$kernel <- "CodeBERT"

codebert_small <- codebert %>% filter(experiment == "small_method_positive")
codebert_small$size <- "< 6" 
codebert_large <- codebert %>% filter(experiment == "large_method_positive")
codebert_large$size <- "> 10" 


round(sapply(cols, function(col) mean(codebert_small[[col]])), 2)
round(sapply(cols, function(col) mean(codebert_large[[col]])), 2)
# Codebert Small 0.75    0.76    0.72    0.53 
# Codebert Large 0.42    0.39    0.45   0.13 

combined_data <- bind_rows(ptk_lessThan6LOC, sstk_lessThan6LOC, stk_lessThan6LOC, ptk_moreThan10LOC, sstk_moreThan10LOC, stk_moreThan10LOC, tfidf_small, tfidf_large, codebert_small, codebert_large)

long_data <- combined_data %>%
  rename(`MAP@100` = MAP) %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, `MAP@100`), 
               names_to = "Metric", 
               values_to = "Value")

rq3_size_plot <- ggplot(long_data, aes(x = kernel, y = Value, fill = size)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  scale_fill_manual(values = c("> 10" = "darkslategray3", "< 6" = "pink")) + 
  labs(y = "", x = "", fill = "LOC") +    # Set legend title for "fill" aesthetic
  theme_minimal() +
  theme(
    legend.title = element_text(),           # Customize the legend title appearance
    axis.text.x = element_text(angle = 45, hjust = 1)  
    )

ggsave("~/treekernel-jss2025/plots/rq3/rq3-LOC-plot-kelp-tfidf-codebert.pdf",rq3_size_plot,width=6,height=5)
