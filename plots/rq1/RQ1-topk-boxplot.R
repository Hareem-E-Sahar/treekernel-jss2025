library(dplyr)
library(ggplot2)   
library(tidyr)
source("~/treekernel-jss2025/plots/rq1/RQ1-load-kernel-metrics.R")   #Prec@5","Prec@10","MRR","MAP","kernel","time","seed","mode"
source("~/treekernel-jss2025/plots/rq1/RQ1-load-baseline-metrics.R") 

topk_all <- topk_all %>% select(1,2,3,4,5) #Prec@5, Prec@10, MRR, MAP, kernel
combined_data <-  bind_rows(topk_all, codebert_rq1, tfidf)

long_data <- combined_data %>%
  rename(`MAP@100` = MAP) %>%
  pivot_longer(
    cols = c(`Prec@5`, `Prec@10`, MRR, `MAP@100`),
    names_to = "Metric",
    values_to = "Value"
  )

rq1_topk<-ggplot(long_data, aes(x = kernel, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  labs( y = "",x = "") +
  scale_fill_manual(values = c("PTK" = "lightblue",  "SSTK"="lightgreen", "STK" = "grey","TF-IDF"="pink","CodeBERT"="khaki")) +
  theme_minimal() +
  theme(legend.title = element_blank(),
        axis.text.x = element_blank())
ggsave("~/treekernel-jss2025/plots/rq1/rq1-topk.pdf", rq1_topk, width=5,height=4)

pvals <- c(
  wilcox.test(ptk_topk$`Prec@5`,  stk_topk$`Prec@5`,        paired=TRUE)$p.value,
  wilcox.test(ptk_topk$`Prec@5`,  sstk_topk$`Prec@5`,       paired=TRUE)$p.value, 
  wilcox.test(stk_topk$`Prec@5`,  sstk_topk$`Prec@5`,       paired=TRUE)$p.value,
  wilcox.test(ptk_topk$`Prec@5`,  tfidf$`Prec@5`,           paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@5`,  tfidf$`Prec@5`,           paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`Prec@5`, tfidf$`Prec@5`,           paired=TRUE)$p.value,
  wilcox.test(ptk_topk$`Prec@5`,  codebert_rq1$`Prec@5`,    paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@5`,  codebert_rq1$`Prec@5`,    paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`Prec@5`, codebert_rq1$`Prec@5`,    paired=TRUE)$p.value
)
p.adjust(pvals, method = "holm") 
0.01757812 0.11132812 0.01757812 0.12890625 0.01757812 0.16015625 0.01757812 0.01757812 0.01757812

p.adjust(pvals, method = "bonferroni") p<0.05
0.01757812 0.33398438 0.01757812 0.58007812 0.01757812 1.00000000 0.01757812 0.01757812 0.01757812
Null: There is no difference between metrics A of two approaches.
Reject null in all except: 
PTK - SSTK
PTK - tfidf
SSTK- tfidf

pvals <- c(
  wilcox.test(ptk_topk$`Prec@10`,  stk_topk$`Prec@10`,        paired=TRUE)$p.value,
  wilcox.test(ptk_topk$`Prec@10`,  sstk_topk$`Prec@10`,       paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@10`,  sstk_topk$`Prec@10`,       paired=TRUE)$p.value,
  wilcox.test(ptk_topk$`Prec@10`,  tfidf$`Prec@10`,                 paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@10`,  tfidf$`Prec@10`,                 paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`Prec@10`, tfidf$`Prec@10`,                paired=TRUE)$p.value,
  wilcox.test(ptk_topk$`Prec@10`,  codebert_rq1$`Prec@10`,         paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@10`,  codebert_rq1$`Prec@10`,         paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`Prec@10`, codebert_rq1$`Prec@10`,        paired=TRUE)$p.value
)
p.adjust(pvals, method = "holm") 
0.01757812 0.38671875 0.01757812 0.31640625 0.01757812 0.43164062 0.01757812 0.01757812 0.01757812
Reject null in all except 
PTK - SSTK
PTK - tfidf
SSTK- tfidf

p.adjust(pvals, method = "bonferroni") 
0.01757812 1.00000000 0.01757812 0.94921875 0.01757812 1.00000000 0.01757812 0.01757812 0.01757812
Reject null in all except 
PTK - SSTK
PTK - tfidf
SSTK- tfidf


pvals <- c(
  wilcox.test(ptk_topk$MRR,  stk_topk$MRR,        paired=TRUE)$p.value,
  wilcox.test(ptk_topk$MRR,  sstk_topk$MRR,       paired=TRUE)$p.value,
  wilcox.test(stk_topk$MRR,  sstk_topk$MRR,       paired=TRUE)$p.value,
  wilcox.test(ptk_topk$MRR,  tfidf$MRR,           paired=TRUE)$p.value,
  wilcox.test(stk_topk$MRR,  tfidf$MRR,           paired=TRUE)$p.value,
  wilcox.test(sstk_topk$MRR, tfidf$MRR,           paired=TRUE)$p.value,
  wilcox.test(ptk_topk$MRR,  codebert_rq1$MRR,    paired=TRUE)$p.value,
  wilcox.test(stk_topk$MRR,  codebert_rq1$MRR,    paired=TRUE)$p.value,
  wilcox.test(sstk_topk$MRR, codebert_rq1$MRR,    paired=TRUE)$p.value
)
p.adjust(pvals, method = "holm") 
0.01757812 0.02929688 0.01757812 0.12890625 0.01757812 0.23242188 0.01757812 0.01757812 0.01757812
Reject null in all except
PTK  - TFIDF
SSTK - TFIDF

p.adjust(pvals, method = "bonferroni") 
0.01757812 0.08789062 0.01757812 0.58007812 0.01757812 1.00000000 0.01757812 0.01757812 0.01757812
Reject null in all except 
PTK - SSTK
PTK  - TFIDF
SSTK - TFIDF


pvals <- c(
  wilcox.test(ptk_topk$MAP,  stk_topk$MAP,        paired=TRUE)$p.value,
  wilcox.test(ptk_topk$MAP,  sstk_topk$MAP,       paired=TRUE)$p.value,
  wilcox.test(stk_topk$MAP,  sstk_topk$MAP,       paired=TRUE)$p.value,
  wilcox.test(ptk_topk$MAP,  tfidf$MAP,           paired=TRUE)$p.value,
  wilcox.test(stk_topk$MAP,  tfidf$MAP,           paired=TRUE)$p.value,
  wilcox.test(sstk_topk$MAP, tfidf$MAP,           paired=TRUE)$p.value,
  wilcox.test(ptk_topk$MAP,  codebert_rq1$MAP,    paired=TRUE)$p.value,
  wilcox.test(stk_topk$MAP,  codebert_rq1$MAP,    paired=TRUE)$p.value,
  wilcox.test(sstk_topk$MAP, codebert_rq1$MAP,    paired=TRUE)$p.value
)
p.adjust(pvals, method = "holm") 
0.01757812 0.69531250 0.01757812 0.07812500 0.19335938 0.32031250 0.01757812 0.01757812 0.01757812
reject null in all except following cases
PTK-SSTK
SSTK-TFIDF

p.adjust(pvals, method = "bonferroni") 
0.01757812 1.00000000 0.01757812 0.17578125 0.58007812 1.00000000 0.01757812 0.01757812 0.01757812
Reject null in all  except 
PTK-SSTK
PTK-TFIDF
SSTK-TFIDF

#compare raw p-values to α / 9 = 0.0056, or

#compare Bonferroni-adjusted p-values to α = 0.05
0.0176 < 0.05
