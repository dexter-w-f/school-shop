<template>
  <div class="front-container" style="width: 90%;">
    <div class="card" style="padding: 25px;">
      <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px;">发布帖子</div>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="data.form.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="data.form.category" placeholder="请选择分类" style="width: 200px;">
            <el-option label="经验分享" value="经验分享"></el-option>
            <el-option label="问答求助" value="问答求助"></el-option>
            <el-option label="闲聊交流" value="闲聊交流"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div style="border: 1px solid #ddd; width: 100%;">
            <Toolbar style="border-bottom:1px solid #ccc" :editor="editorRef" :mode="mode" />
            <Editor style="height: 400px;overflow-y:hidden;" v-model="data.form.content"
                    :mode="mode" :defaultConfig="editorConfig" @onCreated="handleCreated" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="data.saving">发布</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, ref, shallowRef, onBeforeUnmount} from "vue";
import {useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import '@wangeditor/editor/dist/css/style.css';
import {Editor, Toolbar} from '@wangeditor/editor-for-vue';

const router = useRouter();
const formRef = ref(null);
const editorRef = shallowRef();
const mode = 'default';
const editorConfig = { MENU_CONF: {} };

const handleCreated = (editor) => { editorRef.value = editor };
onBeforeUnmount(() => { const editor = editorRef.value; if (editor) editor.destroy(); });

const data = reactive({
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  form: { title: "", category: "", content: "" },
  saving: false,
  rules: {
    title: [{ required: true, message: "请输入标题", trigger: "blur" }],
    category: [{ required: true, message: "请选择分类", trigger: "change" }],
    content: [{ required: true, message: "请输入内容", trigger: "blur" }]
  }
});

const save = () => {
  formRef.value.validate(valid => {
    if (!valid) return;
    data.saving = true;
    data.form.userId = data.user.id;
    data.form.userName = data.user.name;
    data.form.userAvatar = data.user.avatar;
    request.post("/post/add", data.form).then(res => {
      if (res.code === "200") { ElMessage.success("发布成功"); router.push("/front/postList"); }
      else { ElMessage.error(res.msg); }
    }).finally(() => { data.saving = false; });
  });
};
</script>
