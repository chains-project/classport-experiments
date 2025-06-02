import json
import sys

def extract_scores(data):
    no_agent_score = None
    with_agent_score = None

    for entry in data:
        benchmark_name = entry.get("benchmark", "")
        score = entry.get("primaryMetric", {}).get("score", None)

        if score is None:
            continue

        if "no_agent" in benchmark_name:
            no_agent_score = score
        elif "with_agent" in benchmark_name:
            with_agent_score = score

    return no_agent_score, with_agent_score

def main():
    if len(sys.argv) != 2:
        print(f"Usage: python {sys.argv[0]} <jmh-result.json>")
        sys.exit(1)

    filename = sys.argv[1]

    try:
        with open(filename) as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error reading JSON file: {e}")
        sys.exit(1)

    no_agent, with_agent = extract_scores(data)

    if no_agent is None or with_agent is None:
        print("Error: Could not find both 'no_agent' and 'with_agent' benchmarks.")
        sys.exit(1)

    overhead = ((with_agent - no_agent) / no_agent) * 100
    print(f"Without agent: {no_agent:.3f} ms/op")
    print(f"With agent:    {with_agent:.3f} ms/op")
    print(f"Overhead:      {overhead:.2f}%")

if __name__ == "__main__":
    main()
