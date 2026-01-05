library(dplyr)
library(ggplot2)   
library(tidyr)

tfidf<-read.csv("~/treekernel-jss2025/results_final/RQ1/tfidf/metrics_RQ1_TFIDF.csv", header=FALSE)
colnames(tfidf)<-c("Prec@5","Prec@10","MRR","MAP","kernel","time","seed")
tfidf    <- tfidf%>%select (1,2,3,4,5)     #Prec@5, Prec@10, MRR, MAP, kernel
cols <- c("Prec@5", "Prec@10", "MRR", "MAP")
round(sapply(cols, function(col) mean(tfidf[[col]])),2)

codebert<-read.csv("~/treekernel-jss2025/results_final/codebert-metrics.csv", header=FALSE, skip = 1)
codebert <- codebert[, c(1,2,3,4,5,6,7)]
colnames(codebert) <- c("Prec@5","Prec@10","MRR","MAP@100","MAP","experiment","time")
codebert_rq1 <- codebert %>% filter(experiment == "rq1-pos")
codebert_rq1 <- codebert_rq1 %>% select(1,2,3,4) 
colnames(codebert_rq1) <- c("Prec@5","Prec@10","MRR","MAP")
codebert_rq1$kernel <- "CodeBERT"
round(sapply(cols, function(col) mean(codebert_rq1[[col]])),2) 


#TF-IDF  & 0.66  & 0.61 & 0.80 & 0.35        \\final
#CodeBERT& 0.49  & 0.46 & 0.50 & 0.18 & 0.09 \\final 
