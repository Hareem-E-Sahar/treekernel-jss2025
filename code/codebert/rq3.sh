#!/bin/bash

# Run each experiment 10 times and save output

python3 main.py --experiment high_complexity_positive --repeat 1
python3 main.py --experiment low_complexity_positive --repeat 1
python3 main.py --experiment small_method_positive --repeat 1
python3 main.py --experiment large_method_positive --repeat 1



