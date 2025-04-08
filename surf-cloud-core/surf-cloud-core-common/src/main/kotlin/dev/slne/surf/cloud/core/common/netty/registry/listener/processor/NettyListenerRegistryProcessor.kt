package dev.slne.surf.cloud.core.common.netty.registry.listener.processor

import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.util.*
import dev.slne.surf.cloud.core.common.netty.registry.listener.NettyListenerRegistry
import dev.slne.surf.surfapi.core.api.util.logger
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.aop.framework.autoproxy.AutoProxyUtils
import org.springframework.aop.scope.ScopedObject
import org.springframework.aop.scope.ScopedProxyUtils
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.getBeanNamesForType
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.expression.AnnotatedElementKey
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.context.expression.CachedExpressionEvaluator
import org.springframework.context.expression.MethodBasedEvaluationContext
import org.springframework.core.*
import org.springframework.core.MethodIntrospector.MetadataLookup
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.UndeclaredThrowableException
import java.util.concurrent.CompletionStage
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler as Handler


@Component
class NettyListenerRegistryProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is AopInfrastructureBean) {
            val targetClass = bean.ultimateTargetClass()
            if (targetClass.isCandidateFor<Handler>()) {
                val nettyHandlers = targetClass.selectFunctions { it.isAnnotated<Handler>() }
                if (nettyHandlers.isNotEmpty()) {
                    registerNettyHandlers(beanName, bean, nettyHandlers)
                }
            }
        }

        return bean
    }

    private fun registerNettyHandlers(beanName: String, bean: Any, nettyHandlers: Set<Method>) {
        try {
            for (handler in nettyHandlers) {
                NettyListenerRegistry.registerListener(handler, bean)
            }
        } catch (e: SurfNettyListenerRegistrationException) {
            throw BeanCreationException(beanName, e.message ?: "<null>", e)
        }
    }
}

class Test : SmartInitializingSingleton, ApplicationContextAware, BeanFactoryPostProcessor {
    private var context: ConfigurableApplicationContext? = null
    private var beanFactory: ConfigurableListableBeanFactory? = null
    private var eventListenerFactories: ObjectList<NettyListenerFactory>? = null

    private val originalEvaluationContext = StandardEvaluationContext()
    private val evaluator = NettyEventExpressionEvaluator(originalEvaluationContext)

    private val nonAnnotatedClasses = mutableObjectSetOf<Class<*>>()

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        require(applicationContext is ConfigurableApplicationContext) { "ApplicationContext must be ConfigurableApplicationContext" }
        context = applicationContext
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        this.beanFactory = beanFactory
        this.originalEvaluationContext.setBeanResolver(BeanFactoryResolver(beanFactory))

        val beans = beanFactory.getBeansOfType<NettyListenerFactory>(
            includeNonSingletons = false,
            allowEagerInit = false
        )

        val elements = beans.values.toTypedArray()
        AnnotationAwareOrderComparator.sort(elements)
        eventListenerFactories = objectListOf(*elements)
    }

    override fun afterSingletonsInstantiated() {
        val beanFactory = this.beanFactory ?: error("No ConfigurableListableBeanFactory set")
        val beanNames = beanFactory.getBeanNamesForType<Any>()

        for (name in beanNames) {
            if (ScopedProxyUtils.isScopedTarget(name)) continue
            var type =
                runCatching { AutoProxyUtils.determineTargetClass(beanFactory, name) }.getOrNull()
                    ?: continue

            if (ScopedObject::class.java.isAssignableFrom(type)) {
                runCatching {
                    val targetClass = AutoProxyUtils.determineTargetClass(
                        beanFactory,
                        ScopedProxyUtils.getTargetBeanName(name)
                    )
                    if (targetClass != null) {
                        type = targetClass
                    }
                }
            }

            try {
                processBean(name, type)
            } catch (e: Throwable) {
                throw BeanCreationException(
                    "Failed to process @${Handler::class.simpleName} annotation on bean with name '$name': ${e.message}",
                    e
                )
            }
        }
    }

    private fun processBean(beanName: String, targetType: Class<*>) {
        if (this.nonAnnotatedClasses.contains(targetType)) return
        if (!AnnotationUtils.isCandidateClass(targetType, Handler::class.java)) return
        if (isSpringContainerClass(targetType)) return

        var annotatedMethods = runCatching {
            MethodIntrospector.selectMethods(
                targetType,
                MetadataLookup {
                    AnnotatedElementUtils.findMergedAnnotation(
                        it,
                        Handler::class.java
                    )
                }
            )
        }.getOrNull()

        if (annotatedMethods.isNullOrEmpty()) {
            nonAnnotatedClasses.add(targetType)
        } else {
            val context = this.context ?: error("No ConfigurableApplicationContext set")
            val factories = eventListenerFactories ?: error("No NettyListenerFactory beans found")

            for (method in annotatedMethods.keys) {
                for (factory in factories) {
                    if (factory.supportsMethod(method)) {
                        val methodToUse =
                            AopUtils.selectInvocableMethod(method, context.getType(beanName))
                        val listener =
                            factory.createApplicationListener(beanName, targetType, methodToUse)
                        dev.slne.surf.cloud.core.common.netty.registry.listener.processor.NettyListenerRegistry.registerListener(
                            listener
                        )
                        break
                    }
                }
            }
        }
    }

    companion object {

        /**
         * Determine whether the given class is an {@code org.springframework}
         * bean class that is not annotated as a user or test {@link Component}...
         * which indicates that there is no {@link EventListener} to be found there.
         */
        private fun isSpringContainerClass(clazz: Class<*>): Boolean =
            clazz.getName().startsWith("org.springframework.")
                    && !AnnotatedElementUtils.isAnnotated(
                ClassUtils.getUserClass(clazz), Component::class.java
            )
    }
}

interface NettyListenerFactory {
    fun supportsMethod(method: Method): Boolean
    fun createApplicationListener(
        beanName: String,
        type: Class<*>,
        method: Method
    ): NettyListener<*>
}

@Component
class DefaultNettyListenerFactory : NettyListenerFactory, Ordered {
    private var order = Ordered.LOWEST_PRECEDENCE

    fun setOrder(order: Int) {
        this.order = order
    }

    override fun getOrder(): Int {
        return order
    }

    override fun supportsMethod(method: Method): Boolean {
        return true
    }

    override fun createApplicationListener(
        beanName: String,
        type: Class<*>,
        method: Method
    ): NettyListener<*> {
        return NettyListenerMethodAdapter(beanName, type, method)
    }
}

interface NettyListener<P : NettyPacket> {
    suspend fun onPacket(packet: P)
}

interface SmartNettyListener : NettyListener<NettyPacket>, Ordered {
    val listenerId: String
        get() = ""

    fun supportsPacketType(type: Class<*>): Boolean

    fun supportsSourceType(sourceType: Class<*>?): Boolean {
        return true
    }

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }
}

interface GenericNettyListener : SmartNettyListener {
    override fun supportsPacketType(type: Class<*>): Boolean {
        return supportsPacketType(ResolvableType.forClass(type))
    }

    fun supportsPacketType(type: ResolvableType): Boolean
}

class NettyListenerMethodAdapter(
    private val beanName: String,
    targetClass: Class<*>,
    method: Method
) : GenericNettyListener {

    private val method = BridgeMethodResolver.findBridgedMethod(method)
    private val targetMethod = if (!Proxy.isProxyClass(targetClass)) AopUtils.getMostSpecificMethod(
        method,
        targetClass
    ) else method
    private val methodKey = AnnotatedElementKey(targetMethod, targetClass)

    private val annotation =
        AnnotatedElementUtils.findMergedAnnotation(targetMethod, Handler::class.java)

    private val declaredEventTypes = resolveDeclaredPacketTypes(method, annotation)
    private val condition = annotation?.condition
    private val order = resolveOrder(method)

    @Volatile
    private var _listenerId: String?

    private var applicationContext: ApplicationContext? = null
    private var evaluator: NettyEventExpressionEvaluator? = null

    private val coroutineContext: CoroutineContext by lazy {
        Dispatchers.Default + CoroutineName("netty-packet-handler-${listenerId}") + SupervisorJob() + CoroutineExceptionHandler { _, throwable ->
            log.atSevere()
                .withCause(throwable)
                .log("Unhandled exception in NettyListener method")
        }
    }

    override val listenerId: String
        get() {
            var id = this._listenerId
            if (id == null) {
                id = getDefaultListenerId()
                this._listenerId = id
            }
            return id
        }

    init {
        val id = annotation?.id ?: ""
        _listenerId = if (id.isNotEmpty()) id else null
    }

    fun init(applicationContext: ApplicationContext, evaluator: NettyEventExpressionEvaluator) {
        this.applicationContext = applicationContext
        this.evaluator = evaluator
    }

    override suspend fun onPacket(packet: NettyPacket) {
        processPacket(packet)
    }

    override fun supportsPacketType(type: ResolvableType): Boolean {
        for (declaredType in declaredEventTypes) {
            if (if (type.hasUnresolvableGenerics()) declaredType.toClass()
                    .isAssignableFrom(type.toClass()) else declaredType.isAssignableFrom(type)
            ) {
                return true
            }
        }

        return false
    }

    override fun supportsPacketType(type: Class<*>): Boolean {
        return true
    }

    override fun supportsSourceType(sourceType: Class<*>?): Boolean {
        return true
    }

    override fun getOrder(): Int {
        return order
    }

    fun getDefaultListenerId(): String {
        val method = targetMethod
        val joinedParams = method.parameterTypes.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        ) { it.name }

        return ClassUtils.getQualifiedMethodName(method) + joinedParams
    }

    fun processPacket(packet: NettyPacket) {
        val args = resolveArguments(packet)
        if (shouldHandle(packet, args)) {
            val result = doInvoke(args)
            if (result != null) {
                handleResult(result)
            }
        }
    }

    fun shouldHandle(packet: NettyPacket): Boolean {
        return shouldHandle(packet, resolveArguments(packet))
    }

    @OptIn(ExperimentalContracts::class)
    private fun shouldHandle(packet: NettyPacket, args: Array<Any?>?): Boolean {
        contract {
            returns(true) implies (args != null)
        }

        if (args == null) return false
        val condition = condition

        if (!condition.isNullOrBlank()) {
            val evaluator = this.evaluator ?: error("No NettyEventExpressionEvaluator set")
            return evaluator.condition(condition, packet, this.targetMethod, this.methodKey, args)
        }

        return true
    }

    private fun resolveArguments(packet: NettyPacket): Array<Any?>? {
        getResolvableType(packet) ?: return null

        if (method.parameterCount == 0) {
            return arrayOf()
        }

        return arrayOf(packet)
    }

    private fun handleResult(result: Any) {
        if (ReactiveResultHandler().subscribeToPublisher(result)) {
            return
        } else if(result is CompletionStage<*>) {
            result.whenComplete { returnValue, ex ->
                if (ex != null) {
                    log.atSevere().withCause(ex).log("Error in NettyListener method")
                } else {
                    if (returnValue != null && returnValue !is Unit) {
                        error("Unexpected return value from NettyListener method. Expected Unit, got $returnValue")
                    }
                }
            }
        }
    }

    private fun doInvoke(args: Array<Any?>): Any? {
        val bean = getTargetBean()
        if (bean == null) {
            return null
        }

        ReflectionUtils.makeAccessible(this.method)

        try {
            if (KotlinDetector.isSuspendingFunction(this.method)) {
                return CoroutinesUtils.invokeSuspendingFunction(
                    coroutineContext,
                    this.method,
                    bean,
                    args
                )
            }

            return this.method.invoke(bean, *args)
        } catch (ex: IllegalArgumentException) {
            assertTargetBean(this.method, bean, args)
            throw IllegalStateException(getInvocationErrorMessage(bean, ex.message, args))
        } catch (ex: IllegalAccessException) {
            throw IllegalStateException(getInvocationErrorMessage(bean, ex.message, args))
        } catch (ex: InvocationTargetException) {
            val targetException = ex.targetException
            if (targetException is RuntimeException) {
                throw targetException
            }
            val msg =
                getInvocationErrorMessage(bean, "Failed to invoke packet listener method", args)
            throw UndeclaredThrowableException(targetException, msg)
        }
    }

    private fun getTargetBean(): Any? {
        val applicationContext = this.applicationContext ?: error("No ApplicationContext set")
        return applicationContext.getBean(this.beanName)
    }

    private fun getDetailedErrorMessage(bean: Any, message: String?): String {
        return buildString {
            if (message?.isNotEmpty() == true) {
                append(message)
                appendLine()
            }
            append("HandlerMethod details: ")
            appendLine()
            append("Bean [${bean.javaClass.name}]")
            appendLine()
            append("Method [${method.toGenericString()}]")
            appendLine()
        }
    }


    private fun assertTargetBean(method: Method, targetBean: Any, args: Array<Any?>) {
        val methodDeclaringClass = method.declaringClass
        val targetBeanClass: Class<*> = targetBean.javaClass
        if (!methodDeclaringClass.isAssignableFrom(targetBeanClass)) {
            val msg = buildString {
                append("The packet listener method class '")
                append(methodDeclaringClass.getName())
                append("' is not an instance of the actual bean class '")
                append(targetBeanClass.getName())
                append("'. If the bean requires proxying ")
                append("(for example, due to @Transactional), please use class-based proxying.")
            }

            throw IllegalStateException(getInvocationErrorMessage(targetBean, msg, args))
        }
    }

    private fun getInvocationErrorMessage(
        bean: Any,
        message: String?,
        resolvedArgs: Array<Any?>
    ): String {
        return buildString {
            append(getDetailedErrorMessage(bean, message))
            append("Resolved arguments:")
            appendLine()

            for ((i, value) in resolvedArgs.withIndex()) {
                append("[$i] ")
                if (value == null) {
                    append("[null]")
                } else {
                    append("[type=${value.javaClass.name}] ")
                    append("[value=$value]")
                }
                appendLine()
            }
        }
    }

    private fun getResolvableType(packet: NettyPacket): ResolvableType? {
        for (declaredType in declaredEventTypes) {
            val packetClass = declaredType.toClass()
            if (packetClass.isInstance(packet)) {
                return declaredType
            }
        }
        return null
    }

    private inner class ReactiveResultHandler {
        fun subscribeToPublisher(result: Any): Boolean {
            val adapter = ReactiveAdapterRegistry.getSharedInstance().getAdapter(result.javaClass)
            if (adapter != null) {
                adapter.toPublisher<Any>(result).subscribe(PacketListenerPublicationSubscriber())
                return true
            }

            return false
        }
    }

    private inner class PacketListenerPublicationSubscriber: Subscriber<Any> {
        override fun onSubscribe(s: Subscription) {
            s.request(Long.MAX_VALUE)
        }

        override fun onNext(t: Any?) {
            error("Unexpected onNext signal")
        }

        override fun onError(t: Throwable?) {
            log.atSevere().withCause(t).log("Error in NettyListener method")
        }

        override fun onComplete() {

        }
    }

    companion object {
        private val log = logger()

        private fun resolveDeclaredPacketTypes(
            method: Method,
            annotation: Handler?
        ): ObjectList<ResolvableType> {
            val count =
                if (KotlinDetector.isSuspendingFunction(method)) method.parameterCount - 1 else method.parameterCount

            check(count <= 2) { "Maximum of 2 parameters allowed for @${Handler::class.simpleName} method" }
            val params = (0 until count).map { ResolvableType.forMethodParameter(method, it) }
            val packetType = params.find { it.isAssignableFrom(NettyPacket::class.java) }
            val infoType = params.find { it.isAssignableFrom(NettyPacketInfo::class.java) }

            if (annotation != null) {
                val classes = annotation.classes
                if (classes.isNotEmpty()) {
                    val invalidParams =
                        params.filterNot { it.isAssignableFrom(NettyPacketInfo::class.java) }
                    check(invalidParams.isEmpty()) { "When using @${Handler::class.simpleName}(classes), parameters must be empty or only NettyPacketInfo" }
                    check(params.count { it.isAssignableFrom(NettyPacketInfo::class.java) } <= 1) { "Only one NettyPacketInfo parameter allowed" }

                    return classes.mapTo(mutableObjectListOf(classes.size)) {
                        ResolvableType.forClass(
                            it.java
                        )
                    }
                }
            }

            check(count in 1..2) { "Handler method must have 1 or 2 parameters if no classes are specified in the annotation" }
            check(packetType != null) { "Handler method must have a parameter of type NettyPacket" }

            if (params.size == 2) {
                check(infoType != null) { "If two parameters are provided, one must be NettyPacket and the other NettyPacketInfo" }
            }

            return objectListOf(packetType)
        }

        private fun resolveOrder(method: Method): Int {
            val annotation = AnnotatedElementUtils.findMergedAnnotation(method, Order::class.java)
            return annotation?.value ?: Ordered.LOWEST_PRECEDENCE
        }
    }
}


object NettyListenerRegistry {
    fun registerListener(listener: NettyListener<*>) {

    }
}

class NettyEventExpressionEvaluator(private val originalEvaluationContext: StandardEvaluationContext) :
    CachedExpressionEvaluator() {
    private val conditionCache = mutableObject2ObjectMapOf<ExpressionKey, Expression>(64)
        .synchronize()

    fun condition(
        conditionExpression: String,
        packet: NettyPacket,
        targetMethod: Method,
        methodKey: AnnotatedElementKey,
        args: Array<Any?>
    ): Boolean {
        val rootObject = PacketExpressionRootObject(packet, args)
        val evaluationContext = createEvaluationContext(rootObject, targetMethod, args)
        val expression = getExpression(conditionCache, methodKey, conditionExpression)
        val value = expression.getValue(evaluationContext, Boolean::class.java)

        return value == true
    }

    private fun createEvaluationContext(
        rootObject: PacketExpressionRootObject,
        method: Method,
        args: Array<Any?>
    ): EvaluationContext {
        val evaluationContext =
            MethodBasedEvaluationContext(rootObject, method, args, parameterNameDiscoverer)
        this.originalEvaluationContext.applyDelegatesTo(evaluationContext)
        return evaluationContext
    }
}

@JvmRecord
data class PacketExpressionRootObject(
    val packet: NettyPacket,
    val args: Array<Any?>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PacketExpressionRootObject) return false

        if (packet != other.packet) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packet.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }
}