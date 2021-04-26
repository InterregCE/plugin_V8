package io.cloudflight.jems.plugin.standard.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["io.cloudflight.jems.plugin"])
open class PluginConfig
