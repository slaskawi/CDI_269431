package org.myorg.myapp;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import java.util.Set;

/**
 * The error happens while shutting down Weld and it looks like this:
 * <p>
 *     Exception in thread "Thread-1" org.infinispan.jmx.JmxDomainConflictException: ISPN000034: There's already a JMX MBean instance type=CacheManager,name="CDIExtensionDefaultCacheManager" already registered under 'org.infinispan' JMX domain. If you want to allow multiple instances configured with same JMX domain enable 'allowDuplicateDomains' attribute in 'globalJmxStatistics' config element
 *      at org.infinispan.jmx.JmxUtil.buildJmxDomain(JmxUtil.java:52)
 *      at org.infinispan.jmx.CacheManagerJmxRegistration.updateDomain(CacheManagerJmxRegistration.java:79)
 *      at org.infinispan.jmx.CacheManagerJmxRegistration.buildRegistrar(CacheManagerJmxRegistration.java:73)
 *      at org.infinispan.jmx.AbstractJmxRegistration.registerMBeans(AbstractJmxRegistration.java:37)
 *      at org.infinispan.jmx.CacheManagerJmxRegistration.start(CacheManagerJmxRegistration.java:41)
 *      at org.infinispan.manager.DefaultCacheManager.start(DefaultCacheManager.java:657)
 *      at org.infinispan.manager.DefaultCacheManager.<init>(DefaultCacheManager.java:232)
 *      at org.infinispan.manager.DefaultCacheManager.<init>(DefaultCacheManager.java:209)
 *      at org.infinispan.cdi.DefaultEmbeddedCacheManagerProducer.getDefaultEmbeddedCacheManager(DefaultEmbeddedCacheManagerProducer.java:43)
 *      at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 *      at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
 *      at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 *      at java.lang.reflect.Method.invoke(Method.java:497)
 *      at org.jboss.weld.injection.StaticMethodInjectionPoint.invoke(StaticMethodInjectionPoint.java:88)
 *      at org.jboss.weld.injection.StaticMethodInjectionPoint.invoke(StaticMethodInjectionPoint.java:78)
 *      at org.jboss.weld.injection.producer.ProducerMethodProducer.produce(ProducerMethodProducer.java:99)
 *      at org.jboss.weld.injection.producer.AbstractMemberProducer.produce(AbstractMemberProducer.java:161)
 *      at org.jboss.weld.bean.AbstractProducerBean.create(AbstractProducerBean.java:181)
 *      at org.jboss.weld.context.AbstractContext.get(AbstractContext.java:96)
 *      at org.jboss.weld.bean.ContextualInstanceStrategy$DefaultContextualInstanceStrategy.get(ContextualInstanceStrategy.java:101)
 *      at org.jboss.weld.bean.ContextualInstanceStrategy$ApplicationScopedContextualInstanceStrategy.get(ContextualInstanceStrategy.java:141)
 *      at org.jboss.weld.bean.ContextualInstance.get(ContextualInstance.java:50)
 *      at org.jboss.weld.bean.proxy.ContextBeanInstance.getInstance(ContextBeanInstance.java:99)
 *      at org.jboss.weld.bean.proxy.ProxyMethodHandler.getInstance(ProxyMethodHandler.java:125)
 *      at org.infinispan.manager.BasicCacheContainer$CacheContainer$EmbeddedCacheManager$Lifecycle$Listenable$112368021$Proxy$_$$_WeldClientProxy.toString(Unknown Source)
 *      at java.lang.String.valueOf(String.java:2982)
 *      at java.lang.StringBuilder.append(StringBuilder.java:131)
 *      at org.infinispan.cdi.util.Reflections.buildInvokeMethodErrorMessage(Reflections.java:87)
 *      at org.infinispan.cdi.util.Reflections.invokeMethod(Reflections.java:203)
 *      at org.infinispan.cdi.util.Reflections.invokeMethod(Reflections.java:126)
 *      at org.infinispan.cdi.util.InjectableMethod.invoke(InjectableMethod.java:153)
 *      at org.infinispan.cdi.util.InjectableMethod.invoke(InjectableMethod.java:110)
 *      at org.infinispan.cdi.util.defaultbean.DefaultProducerMethod.destroy(DefaultProducerMethod.java:42)
 *      at org.jboss.weld.util.bean.IsolatedForwardingBean.destroy(IsolatedForwardingBean.java:50)
 *      at org.jboss.weld.context.AbstractContext.destroyContextualInstance(AbstractContext.java:147)
 *      at org.jboss.weld.context.AbstractContext.destroy(AbstractContext.java:161)
 *      at org.jboss.weld.context.AbstractSharedContext.destroy(AbstractSharedContext.java:61)
 *      at org.jboss.weld.context.AbstractSharedContext.invalidate(AbstractSharedContext.java:56)
 *      at org.jboss.weld.bootstrap.WeldRuntime.shutdown(WeldRuntime.java:56)
 *      at org.jboss.weld.bootstrap.WeldBootstrap.shutdown(WeldBootstrap.java:113)
 *      at org.jboss.weld.environment.se.WeldContainer.shutdown(WeldContainer.java:237)
 *      at org.jboss.weld.environment.se.WeldContainer$ShutdownHook.run(WeldContainer.java:276)
 * </p>
 *
 * In general the Infinispan Extension creates a default producer for each Injection Point (in our case the IP is located
 * in {@link Kernel#nameCache}). Everything happens in {@link org.infinispan.cdi.util.defaultbean.DefaultBeanExtension}.
 *
 * The class responsible for creating/disposing default Cache Manager beans is {@link org.infinispan.cdi.DefaultEmbeddedCacheManagerProducer}.
 * When Weld is shutting everything down (in shutdown hook) it disposes all beans. The best way to see it is to place debugger break points
 * in {@link org.infinispan.cdi.DefaultEmbeddedCacheManagerProducer#getDefaultEmbeddedCacheManager(Configuration)} and
 * {@link org.infinispan.cdi.DefaultEmbeddedCacheManagerProducer#stopCacheManager(EmbeddedCacheManager)}. The interesting thing happens
 * when the debugger stops in the latter method. If you look carefully at the proxy you will notice that it points to a standard
 * {@link org.infinispan.manager.DefaultCacheManager}. I wonder if this is not a bug... I think I should get a proxy pointing to
 * an instance created by {@link org.infinispan.cdi.DefaultEmbeddedCacheManagerProducer#getDefaultEmbeddedCacheManager(Configuration)}??!!
 */
public class WeldStandaloneTest {

   public static void main(String... args) throws Exception {
      WeldContainer weld = new Weld().initialize();

      Kernel testedBean = weld.instance().select(Kernel.class).get();
      listBeans(weld);

      testedBean.putIntoCache("test", "test");
      System.out.printf("Retrieving entry from Cache: " + testedBean.getFromCache("test"));
   }

   private static void listBeans(WeldContainer weld) {
      BeanManager beanManager = weld.instance().select(BeanManager.class).get();
      Set<Bean<?>> beans = beanManager.getBeans(Object.class,new AnnotationLiteral<Any>() {});
      for (Bean<?> bean : beans) {
         System.out.println(bean.getBeanClass().getName() + " " + bean.getInjectionPoints());
      }
   }

}
