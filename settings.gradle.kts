pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        // ZaloPay SDK repository - COMMENT TẠM THỜI VÌ KHÔNG KẾT NỐI ĐƯỢC
        // Nếu gặp lỗi "Unknown host", thử comment dòng này và kiểm tra kết nối mạng
        // Hoặc thử URL khác: https://repo.zalopay.vn/
        // maven { 
        //     url = uri("https://repo.zalopay.vn/repository/maven-public/")
        //     // Thử bỏ comment dòng sau nếu vẫn lỗi:
        //     // isAllowInsecureProtocol = true
        // }
    }
}

rootProject.name = "DuAn1"
include(":app")
