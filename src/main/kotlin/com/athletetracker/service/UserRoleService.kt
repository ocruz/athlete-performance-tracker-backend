package com.athletetracker.service

import com.athletetracker.entity.User
import com.athletetracker.entity.UserRole
import org.springframework.stereotype.Service

@Service
class UserRoleService {

    fun getDefaultRouteForRole(role: UserRole): String {
        return when (role) {
            UserRole.ADMIN -> "/admin/dashboard"
            UserRole.COACH -> "/coach/dashboard"
            UserRole.ATHLETE -> "/athlete/dashboard"
        }
    }

    fun getDefaultRouteForUser(user: User): String {
        return user.defaultRoute ?: getDefaultRouteForRole(user.role)
    }
}