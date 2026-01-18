package mg.razherana.framework.web.containers;

import java.lang.reflect.Method;

import mg.razherana.framework.web.utils.proxies.annotations.Impl;
import mg.razherana.framework.web.utils.proxies.annotations.Resolve;

public class ResolvContainer {
  private Method implMethod;
  private Impl implAnnotation;

  private Method resolveMethod;
  private Resolve resolveAnnotation;

  private Class<?> mainClass;

  /**
   * @param implMethod
   * @param implAnnotation
   * @param resolveMethod
   * @param resolveAnnotation
   * @param giverClass
   */
  public ResolvContainer(Method implMethod, Impl implAnnotation, Method resolveMethod, Resolve resolveAnnotation,
      Class<?> giverClass) {
    this.implMethod = implMethod;
    this.implAnnotation = implAnnotation;
    this.resolveMethod = resolveMethod;
    this.resolveAnnotation = resolveAnnotation;
    this.mainClass = giverClass;
  }

  /**
   * @return the implMethod
   */
  public Method getImplMethod() {
    return implMethod;
  }

  /**
   * @param implMethod the implMethod to set
   */
  public void setImplMethod(Method implMethod) {
    this.implMethod = implMethod;
  }

  /**
   * @return the implAnnotation
   */
  public Impl getImplAnnotation() {
    return implAnnotation;
  }

  /**
   * @param implAnnotation the implAnnotation to set
   */
  public void setImplAnnotation(Impl implAnnotation) {
    this.implAnnotation = implAnnotation;
  }

  /**
   * @return the resolveMethod
   */
  public Method getResolveMethod() {
    return resolveMethod;
  }

  /**
   * @param resolveMethod the resolveMethod to set
   */
  public void setResolveMethod(Method resolveMethod) {
    this.resolveMethod = resolveMethod;
  }

  /**
   * @return the resolveAnnotation
   */
  public Resolve getResolveAnnotation() {
    return resolveAnnotation;
  }

  /**
   * @param resolveAnnotation the resolveAnnotation to set
   */
  public void setResolveAnnotation(Resolve resolveAnnotation) {
    this.resolveAnnotation = resolveAnnotation;
  }

  /**
   * @return the giverClass
   */
  public Class<?> getMainClass() {
    return mainClass;
  }

  /**
   * @param giverClass the giverClass to set
   */
  public void setMainClass(Class<?> giverClass) {
    this.mainClass = giverClass;
  }

}
