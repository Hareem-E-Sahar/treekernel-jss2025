library(dplyr)
library(ggplot2)   
library(tidyr)

filecols <- c("Prec@5","Prec@10","MRR","MAP","kernel","time","seed","mode")

read_metrics <- function(path, type, filter) {
  df <- read.csv(path, header = FALSE)
  colnames(df) <- filecols
  df <- df[df$mode == "normalized", ]
  df$type   <- type
  df$filter <- filter
  return(df)
}

ptkT1_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T1/metrics_RQ2_T1_PTK_topk.csv",
  type   = "T1",
  filter = "topk"
)

sstkT1_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T1/metrics_RQ2_T1_SSTK_topk.csv",
  type   = "T1",
  filter = "topk"
)

stkT1_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T1/metrics_RQ2_T1_STK_topk.csv",
  type   = "T1",
  filter = "topk"
)

ptkT2_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T2/metrics_RQ2_T2_PTK_topk.csv",
  type   = "T2",
  filter = "topk"
)

sstkT2_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T2/metrics_RQ2_T2_SSTK_topk.csv",
  type   = "T2",
  filter = "topk"
)

stkT2_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T2/metrics_RQ2_T2_STK_topk.csv",
  type   = "T2",
  filter = "topk"
)


ptkT3_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T3/metrics_RQ2_T3_PTK_topk.csv",
  type   = "T3",
  filter = "topk"
)

sstkT3_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T3/metrics_RQ2_T3_SSTK_topk.csv",
  type   = "T3",
  filter = "topk"
)

stkT3_topk <- read_metrics(
  "~/treekernel-jss2025/results_final/RQ2/T3/metrics_RQ2_T3_STK_topk.csv",
  type   = "T3",
  filter = "topk"
)

#round(sapply(cols, function(col) mean(sstkT1_topk[[col]])),2)
#round(sapply(cols, function(col) mean(ptkT1_topk[[col]])),2)


library(dplyr)

topk_all <- bind_rows(
  ptkT1_topk, sstkT1_topk, stkT1_topk,
  ptkT2_topk, sstkT2_topk, stkT2_topk,
  ptkT3_topk, sstkT3_topk, stkT3_topk
)

long_data_topk <- topk_all %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, MAP),
               names_to = "Metric",
               values_to = "Value")

result <- long_data_topk %>%
  group_by(kernel, filter, type, Metric) %>%
  summarise(Mean_Value = mean(Value, na.rm = TRUE), .groups = "drop")

latex_df <- result %>%
  pivot_wider(
    names_from  = Metric,
    values_from = Mean_Value
  )%>%
  select(kernel, type, `Prec@5`, `Prec@10`, MRR, MAP)

library(xtable)
xtable(latex_df)

# kernel type  `Prec@5` `Prec@10`   MRR   MAP
# 1 PTK    T1       0.598     0.483 0.972 0.946
# 2 PTK    T2       0.242     0.159 0.629 0.632
# 3 PTK    T3       0.320     0.280 0.465 0.266
# 4 SSTK   T1       0.598     0.483 0.972 0.946
# 5 SSTK   T2       0.238     0.158 0.621 0.625
# 6 SSTK   T3       0.234     0.208 0.344 0.167
# 7 STK    T1       0.544     0.444 0.881 0.900
# 8 STK    T2       0.294     0.218 0.686 0.630
# 9 STK    T3       0.295     0.250 0.474 0.238

