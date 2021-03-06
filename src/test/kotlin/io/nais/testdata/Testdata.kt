package io.nais.testdata

const val basicNaisYaml = """apiVersion: "nais.io/v1alpha1"
kind: "Application"
metadata:
  name: "mycoolapp"
  namespace: "myteam"
  labels:
    "team": "myteam"
spec:
  image: {{image}}
  liveness:
    path: "/isalive"
    port: 8080
    initialDelay: 20
    timeout: 60
  readiness:
    path: "/isready"
    port: 8080
    initialDelay: 20
    timeout: 60
  replicas:
    min: 2
    max: 2
    cpuThresholdPercentage: 50
  prometheus:
    enabled: true
    path: "/metrics"
  limits:
    cpu: "200m"
    memory: "256Mi"
  requests:
    cpu: "200m"
    memory: "256Mi"
  ingresses:${' '}
    {{#each ingresses as |url|}}
      - {{url}}
    {{/each}}"""
