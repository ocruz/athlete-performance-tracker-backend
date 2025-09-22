package com.athletetracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AthletePerformanceTrackerApplication

fun main(args: Array<String>) {
    runApplication<AthletePerformanceTrackerApplication>(*args)
}