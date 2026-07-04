const nodemailer = require("nodemailer");
const to = process.argv[2], code = process.argv[3];
if (!to || !code) { console.error("参数不足"); process.exit(1); }
const transporter = nodemailer.createTransport({
  host: "smtp.163.com", port: 465, secure: true,
  auth: { user: "15243819650@163.com", pass: "DAuWZYrb9q8HhhUY" }
});
transporter.sendMail({
  from: "15243819650@163.com", to: to,
  subject: "校园小卖部 - 注册验证码",
  html: `<div style="padding:20px;font-family:sans-serif;"><h2>校园小卖部</h2><p>您的注册验证码为：</p><div style="font-size:36px;font-weight:bold;color:#409eff;text-align:center;padding:20px;background:#f5f7fa;border-radius:8px;letter-spacing:6px;">${code}</div><p style="color:#999;">5分钟内有效，请勿泄露。</p></div>`
}).then(() => { process.exit(0); }).catch(e => { console.error(e.message); process.exit(1); });

