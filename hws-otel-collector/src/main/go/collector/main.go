// Program otelcontribcol is an extension to the OpenTelemetry Collector
// that includes additional components, some vendor-specific, contributed
// from the wider community.
package main

import (
	"log"
	"os"
	"path/filepath"
	"strconv"

	"go.opentelemetry.io/collector/component"
	"go.opentelemetry.io/collector/service"
)

func main() {

	if !checkVersionOrHelp(os.Args) {
		log.Println("starting Hardware Sentry OpenTelemetry Collector")
		log.Println("Hardware Sentry OpenTelemetry Collector version:", "${project.version} (Build ${buildNumber} on ${timestamp})")

		err := rollAndRouteLogs()
		if err != nil {
			log.Println("failed to roll and route logs: %v", err)
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
	cmd := service.NewCommand(params)
	if err := cmd.Execute(); err != nil {
		log.Fatalf("collector server run finished with error: %v", err)
	}

	return nil
}

// Roll the log files and redirect the stdout and stderr to the /logs/otel.log file
func rollAndRouteLogs() error {

	ex, err := os.Executable()
	if err != nil {
		return err
	}

	// Get the directory of currently running process
	exPath := filepath.Dir(ex)

	// logs directory
	logsDir := exPath + "/../logs"

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

	maxLogFiles := 3

	// Loop over each file index and remove or rename the file
	for i := maxLogFiles; i >= 0; i-- {

		oldLogFile := buildLogPath(logsDir, i)

		// Old log file doesn't exist? go to the next iteration
		exist, err = exists(oldLogFile)
		if !exist {
			continue
		}

		// Max log files? remove the latest log file, example: otel~3.log
		if i == maxLogFiles {
			err = os.Remove(oldLogFile)
			if err != nil {
				return err
			}
			continue
		}

		// Build the new log file name
		newLogFile := buildLogPath(logsDir, i+1)

		// Rename old file name
		err = os.Rename(oldLogFile, newLogFile)
		if err != nil {
			return err
		}

	}

	logFile := buildLogPath(logsDir, 0)
	redirectLogs(logFile)

	log.Println("redirected output to: " + formatPath(logFile))

	return nil

}

// Build log path assuming that the first log file name is 'otel.log' then otel~number.log
func buildLogPath(dir string, number int) string {
	if number == 0 {
		return dir + "/otel.log"
	}

	return dir + "/otel~" + strconv.Itoa(number) + ".log"

}

// redirect logs to logFile
func redirectLogs(logFile string) {

	// open file read/write | create if not exist | clear file at open if exists
	f, _ := os.OpenFile(logFile, os.O_RDWR|os.O_CREATE|os.O_TRUNC, 0666)

	os.Stdout = f
	os.Stderr = f

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

// Check if arguments have help flag
func checkVersionOrHelp(args []string) bool {
	for _, str := range args {
		if str == "--help" || str == "--version" {
			return true
		}
	}
	return false
}
