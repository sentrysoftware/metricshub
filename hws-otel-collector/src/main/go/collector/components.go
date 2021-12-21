package main

import (
	"go.opentelemetry.io/collector/component"
	"go.opentelemetry.io/collector/consumer/consumererror"

	// extensions
	healthcheckextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/healthcheckextension"
	hwsagentextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/hwsagentextension"
	pprofextension "github.com/open-telemetry/opentelemetry-collector-contrib/extension/pprofextension"
	zpagesextension "go.opentelemetry.io/collector/extension/zpagesextension"

	// receivers
	prometheusexecreceiver "github.com/open-telemetry/opentelemetry-collector-contrib/receiver/prometheusexecreceiver"
	prometheusreceiver "github.com/open-telemetry/opentelemetry-collector-contrib/receiver/prometheusreceiver"
	otlpreceiver "go.opentelemetry.io/collector/receiver/otlpreceiver"

	// exporters
	datadogexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/datadogexporter"
	prometheusexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/prometheusexporter"
	prometheusremotewriteexporter "github.com/open-telemetry/opentelemetry-collector-contrib/exporter/prometheusremotewriteexporter"
	loggingexporter "go.opentelemetry.io/collector/exporter/loggingexporter"

	// processors
	filterprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/filterprocessor"
	metricstransformprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/metricstransformprocessor"
	resourcedetectionprocessor "github.com/open-telemetry/opentelemetry-collector-contrib/processor/resourcedetectionprocessor"
	batchprocessor "go.opentelemetry.io/collector/processor/batchprocessor"
	memorylimiterprocessor "go.opentelemetry.io/collector/processor/memorylimiterprocessor"
)

func components() (component.Factories, error) {
	var errs []error
	var err error
	var factories component.Factories
	factories = component.Factories{}

	extensions := []component.ExtensionFactory{
		zpagesextension.NewFactory(),
		pprofextension.NewFactory(),
		healthcheckextension.NewFactory(),
		hwsagentextension.NewFactory(),
	}
	for _, ext := range factories.Extensions {
		extensions = append(extensions, ext)
	}
	factories.Extensions, err = component.MakeExtensionFactoryMap(extensions...)
	if err != nil {
		errs = append(errs, err)
	}

	receivers := []component.ReceiverFactory{
		otlpreceiver.NewFactory(),
		prometheusexecreceiver.NewFactory(),
		prometheusreceiver.NewFactory(),
	}
	for _, rcv := range factories.Receivers {
		receivers = append(receivers, rcv)
	}
	factories.Receivers, err = component.MakeReceiverFactoryMap(receivers...)
	if err != nil {
		errs = append(errs, err)
	}

	exporters := []component.ExporterFactory{
		loggingexporter.NewFactory(),
		datadogexporter.NewFactory(),
		prometheusremotewriteexporter.NewFactory(),
		prometheusexporter.NewFactory(),
	}
	for _, exp := range factories.Exporters {
		exporters = append(exporters, exp)
	}
	factories.Exporters, err = component.MakeExporterFactoryMap(exporters...)
	if err != nil {
		errs = append(errs, err)
	}

	processors := []component.ProcessorFactory{
		memorylimiterprocessor.NewFactory(),
		batchprocessor.NewFactory(),
		metricstransformprocessor.NewFactory(),
		filterprocessor.NewFactory(),
		resourcedetectionprocessor.NewFactory(),
	}
	for _, pr := range factories.Processors {
		processors = append(processors, pr)
	}
	factories.Processors, err = component.MakeProcessorFactoryMap(processors...)
	if err != nil {
		errs = append(errs, err)
	}

	return factories, consumererror.Combine(errs)
}
