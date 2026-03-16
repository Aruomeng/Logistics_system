import { ElMessageBox, ElMessage } from 'element-plus'
import { verifyPassword } from '../api/user'

/**
 * 安全确认拦截器 (二次密码校验)
 * @param message 提示信息，例如 "确定要删除该用户吗？"
 * @param title 弹窗标题，默认 "高危操作确认"
 * @returns Promise<void> - 只有密码验证通过时才会 resolve，否则 reject
 */
export const secureConfirm = (message: string, title = '高危操作安全性验证'): Promise<void> => {
  return new Promise((resolve, reject) => {
    ElMessageBox.prompt(
      `<div style="margin-bottom: 8px;">${message}</div><div style="font-size: 13px; color: #ef4444; font-weight: 600;">此操作不可逆，请验证登录密码以继续：</div>`,
      title,
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '验证并执行',
        cancelButtonText: '取消操作',
        inputType: 'password',
        inputPlaceholder: '请输入当前账号登录密码',
        inputValidator: (value) => {
          if (!value) {
            return '密码不能为空'
          }
          return true
        },
        type: 'error',
        beforeClose: async (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true
            instance.confirmButtonText = '验证中...'
            try {
              // 发起后端 /password/verify 校验
              await verifyPassword(instance.inputValue)
              done()
            } catch (error: any) {
              // 错误已经被 request.ts 拦截弹出，这里只需阻断关闭
              instance.confirmButtonLoading = false
              instance.confirmButtonText = '验证并执行'
            }
          } else {
            done()
          }
        }
      }
    )
      .then(() => {
        // 密码验证成功
        resolve()
      })
      .catch(() => {
        // 取消了输入
        ElMessage.info('已取消操作')
        reject(new Error('User cancelled secure confirm'))
      })
  })
}
