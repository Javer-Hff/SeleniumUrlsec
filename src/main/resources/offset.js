function getOffset(){
    // 获取背景图及其宽高
    const bg = document.getElementById('slideBg')
    const w = bg.naturalWidth
    const h = bg.naturalHeight
    // 把背景绘制到canvas中，用于获取每个像素的数据
    const cvs = document.createElement('canvas')
    cvs.width = w
    cvs.height = h
    const ctx = cvs.getContext('2d')
    ctx.drawImage(bg, 0, 0)
    // 获取不会收到凹凸影响的某一行：滑块top * 2 + 方块顶部偏移值(23) + 会受到凹凸影响的高度(16) + 1
    // 在该行中寻找符合规则的索引：白 + 黑*87 + 白
    const y = parseInt($('#slideBlock').css('top')) * 2 + 40
    let lastWhite = -1
    for (let x = w / 2; x < w; x ++) {
        const [r, g, b] = ctx.getImageData(x, y, 1, 1).data
        const grey = (r * 299 + g * 587 + b * 114) / 1000
        // 以150为阈值，大于该值的认定为白色
        if (grey > 150) {
            if (lastWhite === -1 || x - lastWhite !== 88) {
                lastWhite = x
            } else {
                lastWhite /= 2 // 图片缩小了2倍
                lastWhite -= 37 // 滑块left(26) + 方块自身偏移值(23 / 2)
                lastWhite >>= 0 // 移动的像素必须为整数
                console.log(lastWhite)
                console.log("=========================================")
                return lastWhite
            }
        }
    }
}
return getOffset();

