// Copyright The OpenTelemetry Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package subprocess

import (
	"time"

	"go.opentelemetry.io/collector/config"
)

const (
	DefaultRestartDelay = 10 * time.Second
	DefaultRetries      = -1
)

// Config has the configuration for the extension.
type Config struct {
	config.ExtensionSettings `mapstructure:",squash"`

	// The path to the executable.
	ExecutablePath string `mapstructure:"executable_path"`

	// Command line arguments.
	Args []string `mapstructure:"args"`

	// Working directory.
	WorkingDirectory string `mapstructure:"working_directory"`

	// Time to wait before restarting the sub process after a failure.
	RestartDelay *time.Duration `mapstructure:"restart_delay"`

	// Number of restarts after failures.
	Retries *int `mapstructure:"retries"`
}
