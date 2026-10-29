# SourceFolio observability

Two different questions, two different places:

- **Is the platform working?** → Prometheus + Grafana + Loki, at `https://grafana.<APP_DOMAIN>`
- **How are portfolios doing?** → inside the product: the author stats and the private health
  report are features, not dashboards. There is no third-party analytics script.

| Question | Where |
| --- | --- |
| Backend / PostgreSQL up? | `up{job=...}` in Grafana; alerts `BackendDown`, `PostgresDown` |
| Can visitors reach the site? | `probe_success{job="blackbox-public"}` — real probes on `/fr`, `/en`, the sitemap and the catalogue API; alert `PublicEndpointDown` |
| Are checks, screenshots and GitHub syncs running? | JobRunr metrics; alerts `BackgroundJobsFailing`, `JobBacklogGrowing`. The JobRunr dashboard is never routed in production |
| Slowest or most failing endpoint | `http_server_requests_seconds_*` by `uri`; p95 via `histogram_quantile` |
| Which errors, when | Loki: `{job="sourcefolio-backend"} \|= "ERROR"` |
| Database pressure | `hikaricp_connections_*`, `pg_stat_database_*` |
| Server and containers (Chromium included) | jobs `node` and `cadvisor`; alerts `NodeMemoryHigh`, `ContainerRestartingFrequently` |
| Did the last deploy break something? | compare error rate and p95 before/after; the version is in `/actuator/info` |

All alert rules live in `prometheus-rules.yml`. They evaluate already; connect an
Alertmanager or a Grafana contact point to receive them.

```
monitoring/
├── prometheus.yml          targets: backend:9090, postgres, node, cadvisor, blackbox
├── prometheus-rules.yml    alert rules
├── blackbox.yml            external HTTP probe module
├── loki.yml                7-day retention
├── config.alloy            /var/log/sourcefolio/*.log → Loki
└── grafana/                provisioned datasources and dashboard
```

Actuator runs on the internal port **9090**: `/actuator/*` is not reachable from the internet,
on purpose; only the `sourcefolio-internal` network sees the metrics.
