import { ElMessage } from 'element-plus'
import router from '../router'
import axios from "axios";

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_URL,
    timeout: 30000  // 后台接口超时时间设置
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    const user = JSON.parse(localStorage.getItem('system-user') || '{}');
    if (user.token) {
        config.headers['token'] = user.token;
        config.headers['X-Current-UserId'] = user.id ?? '';
        // 角色是中文（管理员/普通用户），而 HTTP 请求头只允许 ISO-8859-1 字符，
        // 直接发送会让浏览器抛 "String contains non ISO-8859-1 code point" 并导致整个请求发不出去。
        // 这里做百分号编码，转成纯 ASCII；后端 AuthInterceptor / AdminControllerUtils
        // 的 normalizeRole() 会用 URLDecoder 解回原始角色再比对。
        config.headers['X-Current-Role'] = encodeURIComponent(user.role ?? '');
    }
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        // 当权限验证不通过的时候给出提示
        if (res.code === '401') {
            ElMessage.error(res.msg);
            localStorage.removeItem('system-user');
            router.push("/login")
        }
        return res;
    },
        error => {
        console.log('err' + error)
        return Promise.reject(error)
    }
)


export default request
