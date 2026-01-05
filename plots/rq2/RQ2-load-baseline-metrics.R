library(dplyr)
library(ggplot2)   
library(tidyr)

tfidf<-read.csv("~/treekernel-jss2025/results_final/RQ2/tfidf/metrics_RQ2_TF-IDF.csv",header=FALSE, skip = 1)
colnames(tfidf)<-c("Prec@5","Prec@10", "MRR","MAP","kernel","time","seed","type")
tfidf    <- tfidf%>%select (1,2,3,4,5,8)     #Prec@5, Prec@10, MRR, MAP, kernel, type


long_data <- tfidf %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, MAP),
               names_to = "Metric",
               values_to = "Value")

result <- long_data %>%
  group_by(kernel, type, Metric) %>%
  summarise(Mean_Value = mean(Value, na.rm = TRUE), .groups = "drop")

latex_df <- result %>%
  pivot_wider(
    names_from  = Metric,
    values_from = Mean_Value
  )%>%
  select(kernel, type, `Prec@5`, `Prec@10`, MRR, MAP)

library(xtable)
xtable(latex_df)

# TF-IDF T1       0.52     0.43 0.85 0.83
# TF-IDF T2       0.22     0.14 0.53 0.47
# TF-IDF T3       0.21     0.17 0.38 0.19 

#Precision@5,Precision@10,MRR,MAP@100,MAP,experiment,time
codebert <- read.csv("~/treekernel-jss2025/results_final/codebert-metrics.csv", header=FALSE, skip = 1)
codebert <-codebert %>% select(1,2,3,4,5,6,7)
colnames(codebert)<-c("Prec@5","Prec@10","MRR","MAP@100","MAP","type","time") # type is represented by experiment column for RQ2
codebert_rq2 <- codebert %>% select(1,2,3,4,6) #(MAP@100 but renamed to MAP)
colnames(codebert_rq2) <- c("Prec@5",	"Prec@10", "MRR",	"MAP", "type")
codebert_rq2$kernel <- "CodeBERT"


long_data <- codebert_rq2 %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, MAP),
               names_to = "Metric",
               values_to = "Value")

result <- long_data %>%
  group_by(kernel, type, Metric) %>%
  summarise(Mean_Value = mean(Value, na.rm = TRUE), .groups = "drop")

latex_df <- result %>%
  pivot_wider(
    names_from  = Metric,
    values_from = Mean_Value
  )%>%
  select(kernel, type, `Prec@5`, `Prec@10`, MRR, MAP)

library(xtable)
xtable(latex_df)

# 1 Codebert ST3-VST3                    0.242     0.236 0.255 0.170
# 2 Codebert T1                          0.571     0.465 0.848 0.831
# 3 Codebert T2                          0.221     0.152 0.350 0.407
# 4 Codebert T3                          0.345     0.362 0.319 0.133
# 5 Codebert high_complexity_positive    0.361     0.325 0.434 0.122
# 6 Codebert large_method_positive       0.405     0.373 0.441 0.119
# 7 Codebert low_complexity_positive     0.475     0.456 0.471 0.182
# 8 Codebert rq1-pos                     0.489     0.460 0.505 0.184
# 9 Codebert small_method_positive       0.751     0.756 0.72  0.534
 
