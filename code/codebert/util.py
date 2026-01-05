import os,json
import pandas as pd

def get_method_size(filename):
    # Example: abc_27_35.java
    base = os.path.basename(filename)
    parts = base.split("_")
    if len(parts) < 3:
        raise ValueError(f"Filename format invalid: {filename}")
    try:
        start = int(parts[-2])
        end = int(parts[-1].split(".")[0])  # remove .java
    except ValueError:
         raise ValueError(f"Could not parse line numbers from: {filename}")
    return end - start + 1


def save_metrics(results, filename, experiment, time):
    # If file exists, append without header; else write with header
    if os.path.exists(filename):
        df = pd.DataFrame([results])
        df["experiment"] = experiment
        df["time"] = time     
        df.to_csv(filename, mode="a", header=False, index=False)
    else:
        df = pd.DataFrame([results])
        df["experiment"] = experiment
        df["time"] = time
        df.to_csv(filename, index=False)
    print("Appended results to metrics.csv")


def save_results(results, indices):
    with open("topk_results.json", "w") as f:
        json.dump(results, f, indent=2)

    # Save indices separately if needed
    with open("topk_indices.json", "w") as f:
        json.dump(indices, f, indent=2)

