import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("../gantt_rr.csv")
df = df[df["pid"] != "-"]  # remove CPU ociosa

fig, ax = plt.subplots(figsize=(14, 6))
processos = df["pid"].unique()
ypos = {p: i for i, p in enumerate(processos)}

for _, row in df.iterrows():
    ax.barh(ypos[row["pid"]], row["duracao"], left=row["tempo_inicio"],
            color="steelblue", edgecolor="black")

ax.set_yticks(list(ypos.values()))
ax.set_yticklabels(list(ypos.keys()))
ax.set_xlabel("Tempo")
ax.set_title("Gantt - Round Robin")
plt.tight_layout()
plt.savefig("gantt_rr.png", dpi=150)
