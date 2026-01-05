library(dplyr)
library(ggplot2)
library(tidyr)

source("~/treekernel-jss2025/plots/rq1/RQ1-load-baseline-metrics.R")
source("~/treekernel-jss2025/plots/rq1/RQ1-load-kernel-metrics.R")
source("~/treekernel-jss2025/plots/rq4/RQ4-load-hybrid-metrics.R")

ptk_topk$group  <- "PTK"
hybrid_ptk_topk_norm$group  <- "PTK"
stk_topk$group  <- "STK"
hybrid_stk_topk_norm$group  <- "STK"
sstk_topk$group <- "SSTK"
hybrid_sstk_topk_norm$group <- "SSTK"
tfidf$group     <- "TF-IDF"


## Hybrid

combined_data_rq4_topk <- bind_rows(stk_topk, sstk_topk, ptk_topk, hybrid_stk_topk_norm, hybrid_sstk_topk_norm, hybrid_ptk_topk_norm, tfidf)

combined_data_rq4_topk <- combined_data_rq4_topk %>% select(-mode, -time, -filter)

long_data_rq4_topk <- combined_data_rq4_topk %>%
  rename(`MAP@100` = MAP) %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, `MAP@100`), 
               names_to = "Metric", 
               values_to = "Value")

long_data_rq4_topk <- long_data_rq4_topk %>%
  mutate(GroupKernel = paste(group, kernel))

rq4plot_topk <- ggplot(long_data_rq4_topk, aes(x = factor(paste(group, kernel),
                                                        level = c("STK STK", "SSTK SSTK", "PTK PTK",
                                                                  "STK STK-Hybrid", "SSTK SSTK-Hybrid", 
                                                                  "PTK PTK-Hybrid", "TF-IDF TF-IDF")), 
                                                        y = Value, fill = factor(paste(group, kernel)))) +
  geom_boxplot() + # Removed `fill = "white"` to enable custom colors
  facet_wrap(~ Metric, scales = "free_y") + 
  theme_minimal() +
  theme(
    legend.position = 'none',
    axis.text.x = element_text(angle = 45, hjust = 1),
    axis.title = element_blank()
  ) + 
  scale_x_discrete(
    labels = c(
      "STK STK" = "STK",
      "SSTK SSTK" = "SSTK",
      "PTK PTK" = "PTK",
      "STK STK-Hybrid" = "STK-Hybrid",
      "SSTK SSTK-Hybrid" = "SSTK-Hybrid",
      "PTK PTK-Hybrid" = "PTK-Hybrid",
      "TF-IDF TF-IDF" = "TF-IDF"
    ) ) +
  scale_fill_manual(
    values = c(
      "STK STK" = "#aec7e8",      # Blue for STK
      "SSTK SSTK" = "#ff9896",    # pink for SSTK
      "PTK PTK" = "#98df8a",      # Green for PTK
      "STK STK-Hybrid" = "#aec7e8",  
      "SSTK SSTK-Hybrid" = "#ff9896", 
      "PTK PTK-Hybrid" = "#98df8a",  
      "TF-IDF TF-IDF" = "gray"    # orange for TF-IDF
    ))


ggsave("~/treekernel-jss2025/plots/rq4/RQ4-topk.pdf",rq4plot_topk,width=6,height=4)

combined_data_rq4_topk <- bind_rows(stk_topk, sstk_topk, ptk_topk, hybrid_stk_topk_norm, hybrid_sstk_topk_norm, hybrid_ptk_topk_norm, tfidf)

pvals <- c(
  wilcox.test(ptk_topk$`Prec@5`,              hybrid_ptk_topk_norm$`Prec@5`,        paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@5`,              hybrid_stk_topk_norm$`Prec@5`,       paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`Prec@5`,             hybrid_sstk_topk_norm$`Prec@5`,       paired=TRUE)$p.value, 
  wilcox.test(hybrid_ptk_topk_norm$`Prec@5`,  tfidf$`Prec@5`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_sstk_topk_norm$`Prec@5`, tfidf$`Prec@5`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_stk_topk_norm$`Prec@5`,  tfidf$`Prec@5`,           paired=TRUE)$p.value
 )
#p values = 0.005859375 0.012851240 0.001953125 0.001953125 0.001953125 0.001953125
#pvals<0.05/6 #all are sig except stk

pvals <- c(
  wilcox.test(ptk_topk$`Prec@10`,              hybrid_ptk_topk_norm$`Prec@10`,        paired=TRUE)$p.value,
  wilcox.test(stk_topk$`Prec@10`,              hybrid_stk_topk_norm$`Prec@10`,       paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`Prec@10`,             hybrid_sstk_topk_norm$`Prec@10`,       paired=TRUE)$p.value, 
  wilcox.test(hybrid_ptk_topk_norm$`Prec@10`,  tfidf$`Prec@10`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_sstk_topk_norm$`Prec@10`, tfidf$`Prec@10`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_stk_topk_norm$`Prec@10`,  tfidf$`Prec@10`,           paired=TRUE)$p.value
)
#0.003906250 0.492187500 0.001953125 0.001953125 0.001953125 0.001953125


pvals <- c(
  wilcox.test(ptk_topk$`MRR`,              hybrid_ptk_topk_norm$`MRR`,        paired=TRUE)$p.value,
  wilcox.test(stk_topk$`MRR`,              hybrid_stk_topk_norm$`MRR`,       paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`MRR`,             hybrid_sstk_topk_norm$`MRR`,       paired=TRUE)$p.value, 
  wilcox.test(hybrid_ptk_topk_norm$`MRR`,  tfidf$`MRR`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_sstk_topk_norm$`MRR`, tfidf$`MRR`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_stk_topk_norm$`MRR`,  tfidf$`MRR`,           paired=TRUE)$p.value
)
#0.105468750 0.921875000 0.001953125 0.001953125 0.064453125 0.001953125


pvals <- c(
  wilcox.test(ptk_topk$`MAP`,              hybrid_ptk_topk_norm$`MAP`,        paired=TRUE)$p.value,
  wilcox.test(stk_topk$`MAP`,              hybrid_stk_topk_norm$`MAP`,       paired=TRUE)$p.value,
  wilcox.test(sstk_topk$`MAP`,             hybrid_sstk_topk_norm$`MAP`,       paired=TRUE)$p.value, 
  wilcox.test(hybrid_ptk_topk_norm$`MAP`,  tfidf$`MAP`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_sstk_topk_norm$`MAP`, tfidf$`MAP`,           paired=TRUE)$p.value,
  wilcox.test(hybrid_stk_topk_norm$`MAP`,  tfidf$`MAP`,           paired=TRUE)$p.value
)
#0.003906250  0.625000000 0.048828125 0.001953125 0.001953125 0.001953125
