# RQ3 Complexity - Can update Table 4

library(dplyr)
library(ggplot2)   
library(tidyr)

load_metrics <- function(path, complexity, mycolname) {
  df <- read.csv(path, header = FALSE)
  colnames(df) <- mycolname
  df$complexity <- complexity
  df <- df[df$mode == "Normalized", ]  # keep only normalized rows
  df <- select(df,-mode)
  return(df)
}
mycolname<-c( "Prec@5",  "Prec@10", "MRR",     "MAP",     "kernel",  "time",    "seed",    "mode")
stk_low  <- load_metrics("~/treekernel-jss2025/results_final/RQ3/Low/metrics_RQ3_Low_STK_topk.csv",  "Low",  mycolname)
ptk_low  <- load_metrics("~/treekernel-jss2025/results_final/RQ3/Low/metrics_RQ3_Low_PTK_topk.csv",  "Low",  mycolname)
sstk_low <- load_metrics("~/treekernel-jss2025/results_final/RQ3/Low/metrics_RQ3_Low_SSTK_topk.csv", "Low",  mycolname)


stk_high  <- load_metrics("~/treekernel-jss2025/results_final/RQ3/High/metrics_RQ3_High_STK_topk.csv",  "High", mycolname)
ptk_high  <- load_metrics("~/treekernel-jss2025/results_final/RQ3/High/metrics_RQ3_High_PTK_topk.csv",  "High", mycolname)
sstk_high <- load_metrics("~/treekernel-jss2025/results_final/RQ3/High/metrics_RQ3_High_SSTK_topk.csv", "High", mycolname)

cols <- c("Prec@5", "Prec@10", "MRR", "MAP")
round(sapply(cols, function(col) mean(stk_low[[col]])), 2)
round(sapply(cols, function(col) mean(stk_high[[col]])), 2)

round(sapply(cols, function(col) mean(ptk_low[[col]])), 2)
round(sapply(cols, function(col) mean(ptk_high[[col]])), 2)

round(sapply(cols, function(col) mean(sstk_low[[col]])), 2)
round(sapply(cols, function(col) mean(sstk_high[[col]])), 2)

# New Table 4
# STK  low   0.72    0.69    0.86    0.41   
# PTK  low   0.68    0.63    0.84    0.35   
# SSTK low   0.67    0.62    0.79    0.35   

# STK  High   0.54    0.48    0.72  0.21 
# PTK  High   0.47    0.40    0.67  0.16
# SSTK High   0.42    0.37    0.58  0.17 

mycolname<-c( "Prec@5",  "Prec@10", "MRR",     "MAP",     "kernel",  "time",    "seed",    "mode")

tfidf<-read.csv("~/treekernel-jss2025/results_final/RQ3/metrics_RQ3_TFIDF.csv",header = FALSE)
colnames(tfidf) <- mycolname
tfidf_low<- tfidf[tfidf$mode == "Low", ] 
tfidf_high<- tfidf[tfidf$mode == "High", ] 
tfidf_low$complexity<-"Low"
tfidf_high$complexity<-"High"


cols <- c("Prec@5", "Prec@10", "MRR", "MAP")
round(sapply(cols, function(col) mean(tfidf_low[[col]])), 2)
round(sapply(cols, function(col) mean(tfidf_high[[col]])), 2)
# TF-IDF Low  0.65    0.60    0.79    0.36 
# TF-IDF High 0.61    0.55    0.78    0.28 


codebert<-read.csv("~/treekernel-jss2025/results_final/codebert-metrics.csv")[,1:6]
codebert <- codebert[, -5] #exclude MAP@all
colnames(codebert) <- c("Prec@5", "Prec@10", "MRR", "MAP","experiment")
codebert$kernel <- "CodeBERT"
codebert_high <- codebert %>% filter(experiment == "high_complexity_positive")
codebert_high$complexity <- "High" 
codebert_low<- codebert %>% filter(experiment == "low_complexity_positive")
codebert_low$complexity <- "Low" 

round(sapply(cols, function(col) mean(codebert_low[[col]])), 2)
round(sapply(cols, function(col) mean(codebert_high[[col]])), 2)

# Codebert low  0.48    0.46    0.47    0.18 
# Codebert high 0.36    0.32    0.43    0.12 

combined_data <- bind_rows(ptk_low, sstk_low, stk_low, ptk_high, sstk_high, stk_high, tfidf_low, tfidf_high, codebert_low, codebert_high )

long_data <- combined_data %>%
  rename(`MAP@100` = MAP) %>%
             pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, `MAP@100`), 
               names_to = "Metric", 
               values_to = "Value")


long_data$complexity <- factor(long_data$complexity, levels = c("Low", "High"))

rq3_complexity_plot <- ggplot(long_data, aes(x = kernel, y = Value, fill = complexity)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  scale_fill_manual(values = c("High" = "lightblue", "Low" = "lightgreen")) + 
  labs(y = "", x = "", fill = "Complexity") +    # Set legend title for "fill" aesthetic
  theme_minimal() +
  theme(
    legend.title = element_text(),           # Customize the legend title appearance
    axis.text.x = element_text(angle = 45, hjust = 1)  
  )

ggsave("~/treekernel-jss2025/plots/rq3/rq3-complexity-plot-kelp-tfidf-codebert.pdf",rq3_complexity_plot,width=6,height=5)



