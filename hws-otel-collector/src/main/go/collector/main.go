// Program otelcontribcol is an extension to the OpenTelemetry Collector
// that includes additional components, some vendor-specific, contributed
// from the wider community.
package main

import (
	"fmt"
	"log"
	"os"
	"path/filepath"
	"time"

	"go.opentelemetry.io/collector/component"
	"go.opentelemetry.io/collector/service"
)

const backupTimeFormat = "2006-01-02T15-04-05.000"

func main() {

	homeDir, err := getHomeDir()
	if err != nil {
		log.Println("failed to get the home directory: %v", err)
	} else {
		err = os.Chdir(homeDir)
		if err != nil {
			log.Println("failed to set the working directory: %v", err)
		}
	}

	if !checkVersionOrHelp(os.Args) {
		log.Println("starting Hardware Sentry OpenTelemetry Collector")
		log.Println("Hardware Sentry OpenTelemetry Collector version:", "${project.version} (Build ${buildNumber} on ${timestamp})")

		err := backupOtelLogAtStartup()
		if err != nil {
			log.Println("failed to backup logs: %v", err)
		}
	}

	factories, err := components()
	if err != nil {
		log.Fatalf("failed to build components: %v", err)
	}

	info := component.BuildInfo{
		Command:     "${project.artifactId}",
		Description: "Hardware Sentry OpenTelemetry Collector distribution",
		Version:     "${project.version} (Build ${buildNumber} on ${timestamp})",
	}

	if err := run(service.CollectorSettings{BuildInfo: info, Factories: factories}); err != nil {
		log.Fatal(err)
	}
}

func runInteractive(params service.CollectorSettings) error {
	if err := NewCommand(params).Execute(); err != nil {
		log.Fatalf("collector server run finished with error: %v", err)
	}

	return nil
}

// Backup the otel.log file
func backupOtelLogAtStartup() error {

	logsDir, err := getLogsDir()
	if err != nil {
		return err
	}

	exist, err := exists(logsDir)
	if err != nil {
		return err
	}

	// Create the logs directory if doesn't exist
	if !exist {
		err = os.Mkdir(logsDir, 0666)
		if err != nil {
			return err
		}
	}

	// otel.log
	logFile := buildLogPath(logsDir, false)

	exist, err = exists(logFile)
	// otel.log exist? rename it to 'otel-timestamp.log'. E.g. otel-2022-03-11T18-50-20.292.log
	// We delegate the removal to the lumberjack library which takes care of handling MaxBackups and MaxAge.
	if exist {

		// Build the new log file name
		newLogFile := buildLogPath(logsDir, true)

		// Rename old file name
		err = os.Rename(logFile, newLogFile)
		if err != nil {
			return err
		}

	}

	log.Println("redirected output to: " + logFile)

	return nil

}

// Build log path assuming that the first log file name is 'otel.log' then 'otel-timestamp.log'
func buildLogPath(dir string, withTimestamp bool) string {
	if withTimestamp {
		timestamp := time.Now().Format(backupTimeFormat)
		return filepath.Join(dir, fmt.Sprintf("%s-%s%s", "otel", timestamp, ".log"))
	}

	return filepath.Join(dir, "otel.log")

}

// Check if a file or directory exists
func exists(path string) (bool, error) {
	_, err := os.Stat(path)

	if err == nil {
		return true, nil
	}

	if os.IsNotExist(err) {
		return false, nil
	}
	return false, err
}

// Check if arguments have help or version flags
func checkVersionOrHelp(args []string) bool {
	for _, str := range args {
		if str == "--help" || str == "--version" {
			return true
		}
	}
	return false
}
