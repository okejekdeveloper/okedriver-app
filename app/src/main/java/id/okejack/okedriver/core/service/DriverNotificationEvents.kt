package id.okejack.okedriver.core.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriverNotificationEvents @Inject constructor() {
    private val _events = MutableSharedFlow<NotificationTarget>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()
    fun emit(target: NotificationTarget) { _events.tryEmit(target) }
}
