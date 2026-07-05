<template>
  <div class="front-container" style="width: 90%;">
    <div class="card" style="padding: 25px;" v-if="data.post.id">
      <div style="font-size: 24px; font-weight: bold; margin-bottom: 10px;">
        <el-tag v-if="data.post.isTop === '是'" size="small" type="danger">置顶</el-tag>
        <el-tag v-if="data.post.isEssence === '是'" size="small" type="warning">精华</el-tag>
        {{ data.post.title }}
      </div>
      <div style="font-size: 13px; color: #999; margin-bottom: 20px; display: flex; align-items: center; grid-gap: 15px;">
        <span>{{ data.post.userName }}</span><span>{{ data.post.time }}</span>
        <span>👁 {{ data.post.viewCount }}</span><span>👍 {{ data.post.likeCount }}</span><span>💬 {{ data.post.replyCount }}</span>
      </div>
      <div style="border-top: 1px solid #eee; padding-top: 20px; min-height: 200px; line-height: 1.8;" v-html="data.post.content"></div>
      <div style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;">
        <el-button @click="handleLike" :type="data.liked ? 'primary' : 'default'" size="small">
          {{ data.liked ? '👍 已赞' : '👍 点赞' }} {{ data.post.likeCount }}
      </el-button>
      </div>
    </div>

    <div class="card" style="padding: 25px; margin-top: 15px;" v-if="data.post.id">
      <div style="font-size: 16px; font-weight: bold; margin-bottom: 15px;">回复（{{ data.post.replyCount }}）</div>
      <div v-if="data.replyList.length === 0" style="color: #999; text-align: center; padding: 20px;">暂无回复</div>
      <div v-for="item in data.replyList" :key="item.id" :style="{ padding: '12px', marginBottom: '8px', background: item.parentId ? '#f9f9f9' : '#fff', borderRadius: '5px', marginLeft: item.parentId ? '30px' : '0' }">
        <div style="display: flex; justify-content: space-between; margin-bottom: 5px;">
          <span><b>{{ item.userName }}</b><span v-if="item.parentName" style="color: #999; margin-left: 5px;">回复 @{{ item.parentName }}</span></span>
          <span style="font-size: 12px; color: #999;">{{ item.time }}</span>
        </div>
        <div style="color: #333; line-height: 1.6;">{{ item.content }}</div>
        <div style="margin-top: 5px;"><el-button type="primary" link size="small" @click="replyTo(item)">回复</el-button></div>
      </div>
    </div>

    <div class="card" style="padding: 25px; margin-top: 15px;" v-if="data.post.id">
      <div style="font-size: 16px; font-weight: bold; margin-bottom: 10px;">
        发表回复 <span v-if="data.replyTarget" style="font-size: 13px; color: #999;">回复 @{{ data.replyTarget.userName }}
        <el-button type="danger" link size="small" @click="data.replyTarget = null">取消</el-button></span>
      </div>
      <el-input type="textarea" :rows="4" v-model="data.replyContent" placeholder="写下你的回复..." />
      <el-button type="primary" style="margin-top: 10px;" @click="submitReply" :loading="data.submitting">发布回复</el-button>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, onMounted} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
const route = useRoute(); const router = useRouter();

const data = reactive({
  user: JSON.parse(localStorage.getItem("system-user") || "{}"),
  post: {}, replyList: [], replyContent: "", replyTarget: null, submitting: false, liked: false
})

const loadPost = () => {
  const id = route.query.id;
  if (!id) { router.push("/front/postList"); return; }
  request.get("/post/selectById/" + id).then(res => {
    if (res.data) { data.post = res.data; }
    else { ElMessage.error("帖子不存在"); router.push("/front/postList"); }
  })
}
const loadReplies = () => {
  request.get("/reply/selectAll", { params: { postId: route.query.id } }).then(res => { data.replyList = res.data || []; })
}
const handleLike = () => {
  request.put("/post/like/" + data.post.id, null, { params: { userId: data.user.id } }).then(res => {
    if (res.code === "200") {
      data.liked = !data.liked;
      if (res.data === 1) data.post.likeCount++;
      else if (res.data === -1) data.post.likeCount--;
    }
  });
};
const replyTo = (item) => { data.replyTarget = item; }
const submitReply = () => {
  if (!data.replyContent.trim()) { ElMessage.warning("请输入回复内容"); return; }
  data.submitting = true;
  request.post("/reply/add", { postId: data.post.id, content: data.replyContent, userId: data.user.id, userName: data.user.name, userAvatar: data.user.avatar, parentId: data.replyTarget?.id || null }).then(res => {
    if (res.code === "200") { ElMessage.success("回复成功"); data.replyContent = ""; data.replyTarget = null; loadReplies(); loadPost(); }
    else { ElMessage.error(res.msg); }
  }).finally(() => { data.submitting = false; })
}

onMounted(() => { loadPost(); loadReplies();
  request.put("/post/view/" + route.query.id);
  if (data.user.id) {
    request.get("/post/checkLiked", { params: { postId: route.query.id, userId: data.user.id } }).then(res => { data.liked = res.data === true; });
  }
})
</script>
