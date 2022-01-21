//go:build !windows
// +build !windows

package main

import "go.opentelemetry.io/collector/service"

func run(params service.CollectorSettings) error {
	return runInteractive(params)
}

func formatPath(path string) string {
	return path
}
