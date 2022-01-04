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

//go:build windows

package hwsagentextension

import (
	"os"
	"os/exec"
	"strings"
	"syscall"
)

// Windows version of exec.Command(...)
// Compiles on Windows only
func execCommand(execPath string, args []string) *exec.Cmd {

	var comSpec = os.Getenv("COMSPEC")
	if comSpec == "" {
		comSpec = os.Getenv("SystemRoot") + "\\System32\\cmd.exe"
	}
	cmd := exec.Command(comSpec)                                                                  // #nosec
	cmd.SysProcAttr = &syscall.SysProcAttr{CmdLine: "/C \"" + surroundWithDoubleQuotes(execPath)} // #nosec

	for _, arg := range args {
		cmd.SysProcAttr.CmdLine = cmd.SysProcAttr.CmdLine + " " + surroundWithDoubleQuotes(arg)
	}

	cmd.SysProcAttr.CmdLine = cmd.SysProcAttr.CmdLine + "\""

	return cmd
}

func applyOSSpecificCmdModifications(cmd *exec.Cmd) {}

// If the string value contains whitespaces then surround it with double quotes
func surroundWithDoubleQuotes(value string) string {

	if strings.Contains(value, " ") {
		return "\"" + value + "\""
	}

	return value

}

func getExecutablePath() string {
	return "bin\\hws-agent.cmd"
}
