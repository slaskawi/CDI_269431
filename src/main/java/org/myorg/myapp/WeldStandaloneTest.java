package org.myorg.myapp;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import java.util.Set;

public class WeldStandaloneTest {

   public static void main(String... args) throws Exception {
      WeldContainer weld = new Weld().initialize();

      Kernel testedBean = weld.instance().select(Kernel.class).get();
      listBeans(weld);

      testedBean.putIntoCache("test", "test");
      System.out.printf("Retrieving entry from Cache: " + testedBean.getFromCache("key"));
   }

   private static void listBeans(WeldContainer weld) {
      BeanManager beanManager = weld.instance().select(BeanManager.class).get();
      Set<Bean<?>> beans = beanManager.getBeans(Object.class,new AnnotationLiteral<Any>() {});
      for (Bean<?> bean : beans) {
         System.out.println(bean.getBeanClass().getName());
      }
   }

}
