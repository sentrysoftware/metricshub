package main

import (
	"os"
	"path/filepath"
)

// Get the path of the executable
func executablePath() (string, error) {
	ex, err := os.Executable()
	if err != nil {
		return "", err
	}

	// Get the directory of currently running process
	return filepath.Dir(ex), nil
}

// Get the logs directory
func getLogsDir() (string, error) {
	execPath, err := executablePath()

	if err != nil {
		return "", err
	}

	return filepath.Join(execPath, "..", "logs"), nil
}

// Get the default configuration file path
func getDefaultConfigFile() (string, error) {
	execPath, err := executablePath()

	if err != nil {
		return "", err
	}

	return filepath.Join(execPath, "..", "config", "otel-config.yaml"), nil
}
