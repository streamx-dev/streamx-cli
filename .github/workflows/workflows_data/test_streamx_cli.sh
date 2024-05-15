#!/bin/bash

STREAMX_URL="$1"

error() {
  echo "$@" 1>&2
  exit 1
}

check_http_response() {
    local URL="$1"
    local HTTP_CODE="$2"
    local CONTENT="$3"

    RESPONSE=$(curl -s -w "%{http_code}" "$URL")

    RESPONSE_STATUS="${RESPONSE: -3}"

    echo "Checking '$URL' response"

    if [ "$RESPONSE_STATUS" -eq "$HTTP_CODE" ]; then
        if echo "$RESPONSE" | grep -q "$CONTENT"; then
            echo "Published as expected"
        else
            error "Response does not contain '$CONTENT'"
        fi
    else
        error "Response status code: $RESPONSE_STATUS, expected: $HTTP_CODE"
    fi
}

check_binary_file() {
    local URL="$1"
    local IMAGE_PATH="$2"

    curl -s "$URL" -o "/tmp/downloaded_image.jpg"
    echo "Checking '$URL' file"

    if diff "/tmp/downloaded_image.jpg" "$IMAGE_PATH" >/dev/null; then
    else
        error "Files are different"
    fi
}



streamx publish pages third_param_page.html content/payload.json

check_http_response $STREAMX_URL/third_param_page.html 200 third_param_page

streamx unpublish pages third_param_page.html

check_http_response $STREAMX_URL/third_param_page.html 404



streamx publish pages exact_param_page.html -j '{"content":{"bytes":"exact_param_page"}}'

check_http_response $STREAMX_URL/exact_param_page.html 200 "exact_param_page"

streamx unpublish pages exact_param_page.html

check_http_response $STREAMX_URL/exact_param_page.html 404



streamx publish pages json_path_exact_param_page.html -s content.bytes='Json exact page'

check_http_response $STREAMX_URL/json_path_exact_param_page.html 200 "Json exact page"

streamx unpublish pages json_path_exact_param_page.html

check_http_response $STREAMX_URL/json_path_exact_param_page.html 404



streamx publish pages file_param_page.html -j file://content/file_param_page.json

check_http_response $STREAMX_URL/file_param_page.html 200 "file_param_page"

streamx unpublish pages file_param_page.html

check_http_response $STREAMX_URL/file_param_page.html 404



streamx publish pages json_path_file_param_page.html -s content.bytes=file://content/json_path_file_param_page.json

check_http_response $STREAMX_URL/json_path_file_param_page.html 200 "json_path_file_param_page"

streamx unpublish pages json_path_file_param_page.html

check_http_response $STREAMX_URL/json_path_file_param_page.html 404



# streamx publish assets assets/images/background.png -b 'content.bytes=file://content/binary.png'

# check_binary_file $STREAMX_URL/assets/images/background.png content/binary.png

# streamx unpublish assets assets/images/background.png

# check_http_response $STREAMX_URL/assets/images/background.png 404
