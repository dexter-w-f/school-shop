const nodemailer = require("nodemailer");
const to = process.argv[2], code = process.argv[3];
if (!to || !code) { console.error("参数不足"); process.exit(1); }
// 邮箱账号与授权码从环境变量读取，不要写死在代码里
const smtpUser = process.env.SMTP_USER;
const smtpPass = process.env.SMTP_PASS;
if (!smtpUser || !smtpPass) {
  console.error("未配置 SMTP_USER / SMTP_PASS 环境变量，无法发送邮件");
  process.exit(1);
}
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST || "smtp.163.com",
  port: Number(process.env.SMTP_PORT || 465),
  secure: true,
  auth: { user: smtpUser, pass: smtpPass }
});
transporter.sendMail({
  from: smtpUser, to: to,
  subject: "校园小卖部 - 注册验证码",
  html: `<div style="padding:20px;font-family:sans-serif;"><h2>校园小卖部</h2><p>您的注册验证码为：</p><div style="font-size:36px;font-weight:bold;color:#409eff;text-align:center;padding:20px;background:#f5f7fa;border-radius:8px;letter-spacing:6px;">${code}</div><p style="color:#999;">5分钟内有效，请勿泄露。</p></div>`
}).then(() => { process.exit(0); }).catch(e => { console.error(e.message); process.exit(1); });

