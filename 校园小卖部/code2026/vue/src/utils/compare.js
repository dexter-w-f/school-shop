const COMPARE_KEY = 'compare-items'

export function getCompareList() {
  const raw = localStorage.getItem(COMPARE_KEY)
  return raw ? JSON.parse(raw) : []
}

export function addToCompare(id) {
  const list = getCompareList()
  if (list.length >= 4) {
    return { ok: false, msg: '最多对比4个商品' }
  }
  if (list.includes(id)) {
    return { ok: false, msg: '该商品已在对比列表中' }
  }
  list.push(id)
  localStorage.setItem(COMPARE_KEY, JSON.stringify(list))
  return { ok: true, msg: '已加入对比' }
}

export function removeFromCompare(id) {
  const list = getCompareList().filter(i => i !== id)
  localStorage.setItem(COMPARE_KEY, JSON.stringify(list))
}

export function isInCompare(id) {
  return getCompareList().includes(id)
}

export function clearCompare() {
  localStorage.setItem(COMPARE_KEY, '[]')
}
