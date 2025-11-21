#!/bin/bash
set -e

SOLUTION_URL="$1"

wget -O solution.zip "$SOLUTION_URL"
unzip solution.zip -d solution_dir

SOLUTION_DIR_NAME=$(find solution_dir -mindepth 1 -maxdepth 1 -type d | head -n 1)

cd "$SOLUTION_DIR_NAME"
mvn clean compile -q
