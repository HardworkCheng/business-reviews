#!/bin/bash

# ========================================
# 类目API测试脚本
# ========================================
# 用于测试 /api/common/categories 接口
# ========================================

echo "========================================"
echo "类目API测试"
echo "========================================"
echo ""

# 配置
API_URL="http://localhost:8080/api/common/categories"
TOKEN=""

# 检查是否提供了token
if [ -z "$1" ]; then
    echo "⚠️  警告: 未提供token，将尝试无认证访问"
    echo "用法: ./test-api.sh YOUR_TOKEN_HERE"
    echo ""
else
    TOKEN="$1"
    echo "✓ 使用提供的token进行认证"
    echo ""
fi

# 测试API
echo "1. 测试API端点: $API_URL"
echo "----------------------------------------"

if [ -z "$TOKEN" ]; then
    # 无token访问
    RESPONSE=$(curl -s -w "\n%{http_code}" "$API_URL")
else
    # 带token访问
    RESPONSE=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $TOKEN" "$API_URL")
fi

# 分离响应体和状态码
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP状态码: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" = "200" ]; then
    echo "✓ API调用成功"
    echo ""
    echo "2. 响应数据:"
    echo "----------------------------------------"
    echo "$BODY" | python3 -m json.tool 2>/dev/null || echo "$BODY"
    echo ""
    
    # 验证响应数据
    echo "3. 数据验证:"
    echo "----------------------------------------"
    
    # 检查是否是JSON数组
    if echo "$BODY" | python3 -c "import sys, json; data=json.load(sys.stdin); sys.exit(0 if isinstance(data, list) else 1)" 2>/dev/null; then
        echo "✓ 响应格式正确（JSON数组）"
        
        # 检查数组长度
        COUNT=$(echo "$BODY" | python3 -c "import sys, json; print(len(json.load(sys.stdin)))" 2>/dev/null)
        if [ "$COUNT" = "8" ]; then
            echo "✓ 类目数量正确（8个）"
        else
            echo "✗ 类目数量错误（期望8个，实际${COUNT}个）"
        fi
        
        # 检查必需字段
        HAS_ID=$(echo "$BODY" | python3 -c "import sys, json; data=json.load(sys.stdin); print('yes' if all('id' in item for item in data) else 'no')" 2>/dev/null)
        HAS_NAME=$(echo "$BODY" | python3 -c "import sys, json; data=json.load(sys.stdin); print('yes' if all('name' in item for item in data) else 'no')" 2>/dev/null)
        
        if [ "$HAS_ID" = "yes" ]; then
            echo "✓ 所有类目包含id字段"
        else
            echo "✗ 部分类目缺少id字段"
        fi
        
        if [ "$HAS_NAME" = "yes" ]; then
            echo "✓ 所有类目包含name字段"
        else
            echo "✗ 部分类目缺少name字段"
        fi
        
        # 检查类目名称
        echo ""
        echo "4. 类目名称列表:"
        echo "----------------------------------------"
        echo "$BODY" | python3 -c "import sys, json; data=json.load(sys.stdin); [print(f\"{item.get('id', '?')}. {item.get('name', '?')}\") for item in data]" 2>/dev/null
        
        # 验证类目名称
        echo ""
        echo "5. 类目名称验证:"
        echo "----------------------------------------"
        EXPECTED_NAMES=("美食" "KTV" "美发" "美甲" "足疗" "美容" "游乐" "酒吧")
        NAMES=$(echo "$BODY" | python3 -c "import sys, json; data=json.load(sys.stdin); print(','.join([item.get('name', '') for item in data]))" 2>/dev/null)
        
        for name in "${EXPECTED_NAMES[@]}"; do
            if echo "$NAMES" | grep -q "$name"; then
                echo "✓ 包含类目: $name"
            else
                echo "✗ 缺少类目: $name"
            fi
        done
        
    else
        echo "✗ 响应格式错误（不是JSON数组）"
    fi
    
elif [ "$HTTP_CODE" = "401" ]; then
    echo "✗ 认证失败（401）"
    echo "请提供有效的token: ./test-api.sh YOUR_TOKEN_HERE"
elif [ "$HTTP_CODE" = "404" ]; then
    echo "✗ API端点不存在（404）"
    echo "请检查后端服务是否正确配置"
else
    echo "✗ API调用失败（HTTP $HTTP_CODE）"
    echo ""
    echo "响应内容:"
    echo "$BODY"
fi

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"
