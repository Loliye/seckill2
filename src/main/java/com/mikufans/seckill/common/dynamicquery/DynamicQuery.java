package com.mikufans.seckill.common.dynamicquery;

import java.util.List;

/**
 * 扩展SpringDataJpa 支持东涛jpql/nativesql查询并支持分页查询
 * 使用方法  注入ServiceImpl
 */
public interface DynamicQuery
{
    void save(Object entity);

    void update(Object entity);

    <T> void delete(Class<T> entityClass, Object entityId);

    <T> void delete(Class<T> entityClass, Object[] entityIds);

    /**
     * 查询对象列表  返回list
     *
     * @param nativeSql
     * @param params
     * @param <T>
     * @return
     */
    <T> List<T> nativeQueryList(String nativeSql, Object... params);

    /**
     * 查询对象列表  返回List<Map<key,value>>
     *
     * @param nativeSql
     * @param params
     * @param <T>
     * @return
     */
    <T> List<T> nativeQueryListMap(String nativeSql, Object... params);

    /**
     * 查询对象列表 返回List<组合对象>
     *
     * @param resultClass
     * @param nativeSql
     * @param params
     * @param <T>
     * @return
     */
    <T> List<T> nativeQueryListModel(Class<T> resultClass, String nativeSql, Object... params);

    /**
     * 执行nativeSql统计查询
     *
     * @param nativeSql
     * @param params    占位符参数(例如?1)绑定的参数值
     * @return 统计条数
     */
    Object nativeQueryObject(String nativeSql, Object... params);

    /**
     * 执行nativeSql统计查询
     *
     * @param nativeSql
     * @param params    占位符参数(例如?1)绑定的参数值
     * @return 统计条数
     */
    Object[] nativeQueryArray(String nativeSql, Object... params);

    /**
     * 执行nativeSql的update，delete操作
     *
     * @param nativeSql
     * @param params
     * @return
     */
    int nativeExecuteUpdate(String nativeSql, Object... params);

}
