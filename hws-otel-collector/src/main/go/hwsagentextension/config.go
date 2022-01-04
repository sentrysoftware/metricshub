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

package hwsagentextension

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

	// OTLP gRPC endpoint used by the Hardware Sentry Agent to push metrics.
	Grpc *string `mapstructure:"grpc"`

	// Extra arguments to be passed to the Hardware Sentry Agent.
	ExtraArgs []string `mapstructure:"extra_args"`

	// Time to wait before restarting the Hardware Sentry Agent after a failure.
	RestartDelay *time.Duration `mapstructure:"restart_delay"`

	// Number of restarts after failures.
	Retries *int `mapstructure:"retries"`
}
