library(dplyr)
library(ggplot2)
library(tidyr)
source("~/treekernel-jss2025/plots/rq2/RQ2-topk.R")
source("~/treekernel-jss2025/plots/rq2/RQ2-load-baseline-metrics.R")

tfidf_type1 <- tfidf %>% filter(type == "T1")
tfidf_type2 <- tfidf %>% filter(type == "T2")
tfidf_type3 <- tfidf %>% filter(type == "T3")

codebert_type1 <- codebert_rq2 %>% filter(type == "T1")
codebert_type2 <- codebert_rq2 %>% filter(type == "T2")
codebert_type3 <- codebert_rq2 %>% filter(type == "T3")

combined_typedata <- bind_rows(topk_all, tfidf_type1, tfidf_type2, tfidf_type3, codebert_type1, codebert_type2, codebert_type3)

# long_data_tfidf_codebert <- combined_typedata %>%
#   pivot_longer(cols = c(`Prec@5` ,`Prec@10`, MRR, MAP), 
#                names_to = "Metric", 
#                values_to = "Value")

long_data_tfidf_codebert <- combined_typedata %>%
  rename(`MAP@100` = MAP) %>%
  pivot_longer(
    cols = c(`Prec@5`, `Prec@10`, MRR, `MAP@100`),
    names_to = "Metric",
    values_to = "Value"
  )


rq2_topk <- ggplot(long_data_tfidf_codebert, aes(x = type, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric
  labs(  x = "",y = "") +
  scale_fill_manual(values = c("PTK" = "lightblue", "SSTK" = "lightgreen","STK" = "gray",
                               "TF-IDF"="pink","CodeBERT"="khaki1" )) +
  theme_minimal() +
  theme(legend.title = element_blank(),legend.position = "top")

ggsave("~/treekernel-jss2025/plots/rq2/rq2-topk.pdf", rq2_topk, width=5.5,height=6)
  