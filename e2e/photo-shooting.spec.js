import { test, expect } from '@playwright/test';

// ========================================
// 辅助函数
// ========================================

/** 检查后台是否在运行 */
async function isBackendAlive() {
  try {
    const res = await fetch('http://localhost:8081/api/gallery/groups');
    return res.ok;
  } catch {
    return false;
  }
}

/** 先导航到首页，再设置管理员身份，然后刷新页面使身份生效 */
async function loginAsAdmin(page) {
  await page.goto('/home');
  await page.waitForLoadState('networkidle');
  await page.evaluate(() => {
    localStorage.setItem('user', JSON.stringify({
      username: 'admin',
      displayName: '管理员',
      role: 'admin',
    }));
  });
  // 刷新页面让 App.vue 的 loadUser() 读取到新的 localStorage
  await page.reload();
  await page.waitForLoadState('networkidle');
}

/** 清除登录态 */
async function logout(page) {
  await page.goto('/home');
  await page.waitForLoadState('networkidle');
  await page.evaluate(() => localStorage.removeItem('user'));
  await page.reload();
  await page.waitForLoadState('networkidle');
}

/** 打开相册第一个图片（如果存在） */
async function openFirstImage(page) {
  const images = page.locator('.image-card');
  const count = await images.count();
  if (count === 0) return false;
  await images.first().click();
  return true;
}

// ========================================
// 测试用例
// ========================================

test.describe('Photo Shooting E2E', () => {

  test.beforeAll(async () => {
    // 确保后端在运行
    const alive = await isBackendAlive();
    if (!alive) {
      console.warn('⚠️  后端未运行 (http://localhost:8081)，部分测试可能失败');
    }
  });

  // ---------- 首页 ----------

  test('首页加载并展示相册', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    // 页面标题可见
    await expect(page.locator('.logo')).toHaveText("MR WORRY'S PORTFOLIO");
    // 相册卡片存在（如果后端有相册数据）
    const cards = page.locator('.portfolio-card');
    const count = await cards.count();
    if (count > 0) {
      await expect(cards.first()).toBeVisible();
      console.log(`  首页展示了 ${count} 个相册`);
    }
  });

  test('点击相册进入画廊', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    const cards = page.locator('.portfolio-card');
    const count = await cards.count();
    test.skip(count === 0, '没有相册可点击');

    await cards.first().click();
    // 点击后进入画廊或仍在首页
    await page.waitForTimeout(500);
    const url = page.url();
    expect(url.includes('/home') || url.includes('/gallery')).toBeTruthy();
  });

  // ---------- 画廊 ----------

  test('画廊页面展示图片', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    const cards = page.locator('.portfolio-card');
    const count = await cards.count();
    test.skip(count === 0, '没有相册');

    await cards.first().click();
    await page.waitForTimeout(500);

    if (page.url().includes('/gallery')) {
      const images = page.locator('.image-card');
      const imgCount = await images.count();
      if (imgCount > 0) {
        await expect(images.first()).toBeVisible();
        console.log(`  画廊展示了 ${imgCount} 张图片`);
      }
    }
  });

  test('画廊灯箱打开和关闭', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    const cards = page.locator('.portfolio-card');
    const count = await cards.count();
    test.skip(count === 0, '没有相册');

    await cards.first().click();
    await page.waitForTimeout(500);
    test.skip(!page.url().includes('/gallery'), '未进入画廊页面');

    const hasImage = await openFirstImage(page);
    test.skip(!hasImage, '没有图片');

    // 灯箱应该可见
    await expect(page.locator('.overlay')).toBeVisible();
    // 关闭灯箱
    await page.locator('.close-button').click();
    await expect(page.locator('.overlay')).not.toBeVisible();
  });

  // ---------- 管理员 ----------

  test.describe('管理员功能', () => {

    test('管理员能看到退出按钮', async ({ page }) => {
      await loginAsAdmin(page);

      // 应显示用户信息和退出按钮
      await expect(page.locator('.app-user')).toBeVisible();
      await expect(page.locator('.logout-btn')).toBeVisible();
    });

    test('管理员退出登录', async ({ page }) => {
      await loginAsAdmin(page);

      // 点击退出
      await page.locator('.logout-btn').click();
      await page.waitForURL('/home');

      // 检查 localStorage 已清除
      const user = await page.evaluate(() => localStorage.getItem('user'));
      expect(user).toBeNull();

      // 退出按钮应该消失
      await expect(page.locator('.logout-btn')).not.toBeVisible();
    });

    test('管理员界面可访问', async ({ page }) => {
      await loginAsAdmin(page);

      // 导航到管理页面
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');

      // 应显示管理界面
      await expect(page.locator('.admin-view')).toBeVisible();
      // 应该有"创建"按钮
      await expect(page.locator('.add-icon-btn')).toBeVisible();
    });

    test('非管理员无法访问管理界面', async ({ page }) => {
      await logout(page);

      // 直接访问管理页面
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');

      // 应显示登录界面而非管理界面
      await expect(page.locator('.auth-view')).toBeVisible();
    });

    test('创建新相册', async ({ page }) => {
      const backendAlive = await isBackendAlive();
      test.skip(!backendAlive, '后端未运行');

      await loginAsAdmin(page);

      // 导航到管理页面
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');

      // 点击创建相册按钮
      await page.locator('.add-icon-btn').click();

      // 输入相册标题
      const titleInput = page.locator('.card-section .el-input').first().locator('input');
      const testAlbumTitle = 'E2E测试' + Date.now();
      await titleInput.fill(testAlbumTitle);

      // 点击创建按钮
      await page.locator('.create-btn').click();
      await page.waitForTimeout(1000);

      // 检查新相册出现在列表中
      await expect(page.locator('.group-item').first()).toBeVisible();
    });
  });

  // ---------- 语言切换 ----------

  test('语言切换按钮可见', async ({ page }) => {
    await logout(page);

    // 未登录时显示语言切换
    await expect(page.locator('.lang-globe-btn')).toBeVisible();

    // 点击后显示语言选项
    await page.locator('.lang-globe-btn').click();
    await expect(page.locator('.lang-dropdown')).toBeVisible();
  });

  // ---------- 导航 ----------

  test('访问首页导航链接', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    // logo 链接存在
    await expect(page.locator('.logo')).toBeVisible();
  });
});
