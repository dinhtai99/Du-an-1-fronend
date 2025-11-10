// ============================================
// SCRIPT TEST API BACKEND
// Chạy: node test-backend-api.js
// ============================================

const axios = require('axios');

const BASE_URL = 'http://localhost:3000';

// Test data
const testLogin = {
  tenDangNhap: 'admin',
  matKhau: 'admin123',
  vaiTro: 'admin'
};

async function testLoginAPI() {
  console.log('🧪 Testing Login API...\n');
  console.log('Request URL:', `${BASE_URL}/api/auth/login`);
  console.log('Request Body:', JSON.stringify(testLogin, null, 2));
  console.log('\n---\n');
  
  try {
    const response = await axios.post(`${BASE_URL}/api/auth/login`, testLogin, {
      headers: {
        'Content-Type': 'application/json'
      },
      timeout: 5000
    });
    
    console.log('✅ SUCCESS!');
    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(response.data, null, 2));
    
    // Kiểm tra format response
    if (response.data.success && response.data.data) {
      console.log('\n✅ Response format is CORRECT!');
      console.log('User data:', response.data.data);
    } else {
      console.log('\n⚠️  Response format might be INCORRECT!');
      console.log('Expected: { success: true, message: "...", data: {...} }');
    }
    
  } catch (error) {
    console.log('❌ ERROR!');
    
    if (error.response) {
      // Server trả về response nhưng có lỗi
      console.log('Status:', error.response.status);
      console.log('Response:', JSON.stringify(error.response.data, null, 2));
    } else if (error.request) {
      // Request được gửi nhưng không nhận được response
      console.log('❌ No response from server!');
      console.log('Possible causes:');
      console.log('  - Server is not running');
      console.log('  - Wrong URL/port');
      console.log('  - Network issue');
    } else {
      // Lỗi khi setup request
      console.log('Error:', error.message);
    }
  }
}

// Test server health
async function testServerHealth() {
  console.log('\n🧪 Testing Server Health...\n');
  
  try {
    const response = await axios.get(`${BASE_URL}/`, {
      timeout: 3000
    });
    console.log('✅ Server is running!');
    console.log('Response:', response.data);
  } catch (error) {
    console.log('❌ Server is not responding!');
    console.log('Make sure server is running on port 3000');
  }
}

// Main
async function main() {
  console.log('='.repeat(50));
  console.log('BACKEND API TEST SCRIPT');
  console.log('='.repeat(50));
  console.log('');
  
  // Test server health first
  await testServerHealth();
  
  console.log('\n' + '='.repeat(50) + '\n');
  
  // Test login API
  await testLoginAPI();
  
  console.log('\n' + '='.repeat(50));
  console.log('Test completed!');
  console.log('='.repeat(50));
}

main();

