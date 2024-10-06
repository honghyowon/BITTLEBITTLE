<template>
  <div class="container mt-3">
    <div class="row">
      <!-- 검색 필드 -->
      <div class="col-md-2">
        <button class="btn btn-tag-search" @click="openModal">
          <span class="tag-selection-text">태그 선택</span>
          <span class="arrow-down"></span>
        </button>
      </div>

      <div class="col-md-6">
        <input
          type="text"
          class="form-control"
          v-model="keyword"
          placeholder="검색어를 입력하세요"
        />
      </div>
      <div class="col-md-2">
        <button class="btn btn-primary" @click="search">검색</button>
        <button class="btn btn-primary reset" @click="resetSearch">초기화</button>
      </div>
    </div>

    <!-- 선택한 태그 표시 -->
    <div class="selected-tags mt-2"> <!-- mt-3에서 mt-2로 조정 -->
      <span v-for="(tag, index) in selectedTagLabels" :key="index" class="selected-tag">
        {{ tag.name }}
        <button class="remove-tag" @click="removeTag(tag)">×</button>
      </span>
    </div>


    <!-- 태그 선택 모달 -->
    <div v-if="isModalOpen" class="modal">
      <div class="modal-content">
        <span class="close" @click="closeModal">&times;</span>
        <h4>취향에 맞게 태그를 선택해보세요!</h4>
        <div v-for="tagType in tagTypeList" :key="tagType.tagTypeNo" class="bottle-list">
          <div class="tag-selection">
            <div class="title">
              <span class="tag-type-name">{{ tagType.tagTypeName }}</span>
              <label class="select-all">
                <input
                  type="checkbox"
                  :name="`tag-${tagType.tagTypeNo}`"
                  :value="null"
                  @change="selectAllTags(tagType.tagTypeNo)"
                />
                전체선택
              </label>
            </div>
            <div class="tags">
              <label
                v-for="tag in tagList.filter((tag) => tag.keyTypeNo === tagType.tagTypeNo)"
                :key="tag.tagNo"
                class="tag-box"
              >
                <input
                  type="checkbox"
                  :name="`tag-${tagType.tagTypeNo}`"
                  :value="tag.tagNo"
                  v-model="selectedTags[tagType.tagTypeNo]"
                />
                <span class="tag-label">{{ tag.tagName }}</span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="applyTagFilter">적용</button>
        </div>
      </div>
    </div>
    
    <!-- 보틀 목록 -->
    <div class="grid-container mt-3">
      <div v-for="bottle in paginatedBottles" :key="bottle.bottleNo">
        <router-link
          class="title"
          :to="{
            name: 'BottleDetailView',
            params: { bottleNo: bottle.bottleNo },
          }"
        >
          <img
            :src="getBottleImage(bottle.imgUrl, bottle.imgCusUrl)"
            alt="보틀 이미지"
            width="200"
            height="200"
          />
          <br />
          {{ bottle.bottleName }}
          <img src="@/images/star.png" alt="별 이미지" width="20" height="20" />
          {{ bottle.grade }}
        </router-link>
        <div class="review-count">리뷰 {{ bottle.reviewCount }}</div>
      </div>
    </div>

    <!-- 페이징 버튼 -->
    <div class="pagination">
      <button @click="previousPage" :disabled="currentPage === 1">&lt;</button>
      <span>Page {{ currentPage }} of {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">&gt;</button>
    </div>
  </div>
</template>

<script>
import { getFormAxiosInstance } from '@/api/index'
import { onMounted, ref, computed } from 'vue'
import { useUserStore } from '@/stores/users'

export default {
  name: 'BottleAll',
  setup () {
    const user = useUserStore()
    const axios = getFormAxiosInstance(user.getLoginUserInfo)

    const bottles = ref([])
    const filteredBottles = ref([])
    const favorites = ref([])
    const keyword = ref('')
    const tags = ref([])
    const tagList = ref([])
    const tagTypeList = ref([])
    const selectedTags = ref({})
    const isModalOpen = ref(false)  // 모달 창 상태
    const selectedTagLabels = ref([]) // 선택한 태그 목록


    const currentPage = ref(1) // 현재 페이지 번호
    const itemsPerPage = 12; // 한 페이지당 보틀 수

    onMounted(() => {
      axios
        .get('/api/bottles/all')
        .then((res) => {
          bottles.value = res.data.bottle
          filteredBottles.value = bottles.value // 처음에는 모든 보틀을 표시
          favorites.value = res.data.favorites
        })
        .catch((err) => console.error('Error fetching bottles:', err))

      axios
        .get('/api/tags')
        .then((res) => {
          tags.value = res.data.tags
          tagList.value = res.data.tagList
          tagTypeList.value = res.data.tagTypeList

          // Initialize selectedTags with empty arrays for each tagTypeNo
          tagTypeList.value.forEach((tagType) => {
            selectedTags.value[tagType.tagTypeNo] = []
          })
        })
        .catch((err) => console.error('Error fetching tags:', err))
    })

    const paginatedBottles = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage;
      const end = start + itemsPerPage;
      return filteredBottles.value.slice(start, end);
    });

    const totalPages = computed(() => {
      return Math.ceil(filteredBottles.value.length / itemsPerPage);
    });

    const selectAllTags = (tagTypeNo) => {
      const tagsForType = tagList.value
        .filter((tag) => tag.keyTypeNo === tagTypeNo)
        .map((tag) => tag.tagNo)
      if (selectedTags.value[tagTypeNo].length === tagsForType.length) {
        selectedTags.value[tagTypeNo] = [] // 전체 선택 해제
      } else {
        selectedTags.value[tagTypeNo] = [...tagsForType] // 전체 선택
      }
    }

    const search = () => {
      axios
        .post('/api/bottles/all', {
          keyword: keyword.value,
          tagNoList: Object.values(selectedTags.value).flat()
        })
        .then((res) => {
          filteredBottles.value = res.data.bottle
          currentPage.value = 1; // 검색 후 첫 페이지로 리셋
        })
        .catch((err) => console.error('Error searching bottles:', err))
    }

    const applyTagFilter = () => {
      console.log('select', selectedTags);
      
      // 선택한 태그로 보틀 목록 필터링
      axios
        .post('/api/bottles/all', {
          keyword: keyword.value,
          tagNoList: Object.values(selectedTags.value).flat()
        })
        .then((res) => {
          console.log('result', res.data.bottle);      
          filteredBottles.value = res.data.bottle
          closeModal() // 모달 닫기
          currentPage.value = 1; // 필터 적용 후 첫 페이지로 리셋
          updateSelectedTagLabels(); // 선택한 태그 업데이트
        })
        .catch((err) => console.error('Error applying tag filter:', err))
    }

    const updateSelectedTagLabels = () => {
      selectedTagLabels.value = [] // 기존 선택한 태그 초기화
      for (const tagTypeNo in selectedTags.value) {
        selectedTags.value[tagTypeNo].forEach(tagNo => {
          const tag = tagList.value.find(t => t.tagNo === tagNo)
          if (tag) {
            selectedTagLabels.value.push({ id: tag.tagNo, name: tag.tagName }) // ID와 이름 저장
          }
        })
      }
    }

    const removeTag = (tag) => {
      selectedTagLabels.value = selectedTagLabels.value.filter(t => t.id !== tag.id) // ID로 태그 제거
      for (const tagTypeNo in selectedTags.value) {
        const index = selectedTags.value[tagTypeNo].indexOf(tag.id)
        if (index !== -1) {
          selectedTags.value[tagTypeNo].splice(index, 1) // 체크박스 선택 해제
        }
      }
      applyTagFilter() // 필터 적용
    }


    const getBottleImage = (imgUrl, imgCusUrl) => {
      return imgCusUrl
        ? `http://localhost:8080/BITTLEBITTLE/image?path=bottle&name=${imgCusUrl}`
        : `http://localhost:8080/BITTLEBITTLE/image?path=bottle&name=${imgUrl}`
    }

    const openModal = () => {
      isModalOpen.value = true
    }

    const closeModal = () => {
      isModalOpen.value = false
    }

    const previousPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
      }
    };

    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
      }
    };

    const resetSearch = () => {
      axios
        .get('/api/bottles/all')
        .then((res) => {
          bottles.value = res.data.bottle
          filteredBottles.value = bottles.value // 처음에는 모든 보틀을 표시
          favorites.value = res.data.favorites
        })
        .catch((err) => console.error('Error fetching bottles:', err))

      axios
        .get('/api/tags')
        .then((res) => {
          tags.value = res.data.tags
          tagList.value = res.data.tagList
          tagTypeList.value = res.data.tagTypeList

          // Initialize selectedTags with empty arrays for each tagTypeNo
          tagTypeList.value.forEach((tagType) => {
            selectedTags.value[tagType.tagTypeNo] = []
          })
        })
        .catch((err) => console.error('Error fetching tags:', err))

        selectedTagLabels.value = [];
        keyword.value = '';
    };

    return {
      bottles,
      filteredBottles,
      favorites,
      keyword,
      tagList,
      tagTypeList,
      selectedTags,
      isModalOpen,
      search,
      selectAllTags,
      applyTagFilter,
      openModal,
      closeModal,
      getBottleImage,
      paginatedBottles,
      currentPage,
      totalPages,
      previousPage,
      nextPage,
      selectedTagLabels,
      removeTag,
      updateSelectedTagLabels,
      resetSearch
    }
  }
}
</script>
<style scoped>
/* 모달 스타일 */
.modal {
  display: flex;
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  justify-content: center;
  align-items: center;
  overflow: auto;
}

.modal-content {
  background-color: #fefefe;
  padding: 20px;
  border-radius: 10px;
  width: 90%;
  max-width: 800px;
  max-height: 98vh;
  position: relative;
  overflow-y: auto;
}

.modal-footer {
  display: flex; /* 플렉스 박스로 설정 */
  justify-content: center; /* 가운데 정렬 */
  margin-top: 20px; /* 위쪽 간격 조정 */
  padding: 10px 0; /* 하단 패딩 */
}

.close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 28px;
  font-weight: bold;
  cursor: pointer;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 200px); /* 한 줄에 4개의 아이템 배치 */
  justify-content: center;
  gap: 1rem;
}

/* 태그 선택 영역 스타일 */
.tag-selection {
  margin-bottom: 10px; /* 태그 선택 영역의 하단 간격 조정 */
}

.tags {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 한 줄에 4개의 태그 배치 */
  gap: 10px; /* 태그 간의 간격을 설정 */
  margin-top: 5px; /* 태그 상단 간격 */
}

.tag-box {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 30px;
  background-color: #555;
  color: white;
  border-radius: 5px;
  cursor: pointer;
  font-size: smaller;
}

.tag-box input {
  display: none; /* 체크박스 숨김 */
}

.tag-label {
  width: 100%; /* 전체 너비 사용 */
  height: 100%; /* 전체 높이 사용 */
  display: flex;
  justify-content: center; /* 가운데 정렬 */
  align-items: center; /* 수직 가운데 정렬 */
  background-color: gold; /* 버튼 기본 색상 */
  color: black; /* 글자 색상 */
  border-radius: 5px;
  transition: background-color 0.3s;
  font-weight: bold; /* 글씨 굵게 설정 */
  text-overflow: ellipsis; /* 넘치는 텍스트에 말줄임표 표시 */
  white-space: nowrap; /* 줄바꿈 방지 */
}

.tag-box input:checked + .tag-label {
  background-color: orange; /* 선택 시 색상 */
}
.tag-type-name {
  font-weight: bold; /* 글씨 굵게 설정 */
  color: black; /* 글자 색상 검정색 */
  font-size: 15px;
}
.btn-primary {
  background-color: silver; /* 버튼 기본 색상 */
  color: black; /* 버튼 글자 색상 */
  padding: 5px 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: orange; /* 버튼 호버 시 색상 */
}

/* 검색 영역 스타일 */
.container {
  max-width: 1100px; /* 페이지 전체 최대 너비 */
  margin: 0 auto; /* 콘텐츠를 중앙 정렬 */
  padding: 0 15px; /* 좌우 여백 추가 */
}

.row {
  display: flex;
  justify-content: center; /* 가로로 가운데 정렬 */
  align-items: center; /* 세로로 중앙 정렬 */
  margin: 0; /* 기본 마진 제거 */
}

.col-md-6,
.col-md-2 {
  display: flex;
}

.mt-3 {
  margin-top: 0rem; /* 검색 영역 상단 간격을 0.5rem로 설정 */
}

.mt-2 {
  margin-top: 0rem; /* 검색 필드와 버튼 사이의 간격을 0.25rem로 설정 */
}

.btn-tag-search {
  background-color: black; /* 버튼 배경색 검정색 */
  color: white; /* 글자색 주황색 */
  padding: 10px 15px; /* 패딩 설정 */
  border: none; /* 기본 테두리 제거 */
  border-radius: 5px; /* 모서리 둥글게 */
  cursor: pointer; /* 커서 포인터로 변경 */
  font-size: 14px; /* 기본 폰트 크기 설정 */
  display: flex; /* 플렉스박스로 변경 */
  align-items: center; /* 수직 정렬 */
  justify-content: center; /* 수평 정렬 */
  position: relative; /* 자식 요소 위치 설정 */
  transition: background-color 0.3s; /* 호버 시 색상 전환 효과 */
}

.tag-selection-text {
  font-size: 25px; /* 글자 크기 조정 */
  font-weight: bold; /* 글씨 굵게 설정 */
  margin-right: 8px; /* 화살표와 간격 조정 */
}

.arrow-down {
  width: 0;
  height: 0;
  border-left: 5px solid transparent; /* 왼쪽 화살표 선 */
  border-right: 5px solid transparent; /* 오른쪽 화살표 선 */
  border-top: 5px solid orange; /* 아래쪽 화살표 색상 */
  margin-left: 8px; /* 텍스트와 화살표 간 간격 */
  margin-top: 3px; /* 텍스트 위로 약간 간격 추가 */
}
.review-count {
  color: white; /* 리뷰 글씨 색상 흰색으로 변경 */
}
/* 페이지네이션 버튼 스타일 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px; /* 버튼과 다른 요소 간의 간격 추가 */
}

.pagination button {
  background: none; /* 배경 제거 */
  color: white; /* 글자 색상 흰색 */
  border: none; /* 테두리 제거 */
  cursor: pointer; /* 포인터 커서 */
  font-size: 16px; /* 글자 크기 조정 */
  margin: 0 10px; /* 버튼 간격 */
  transition: color 0.3s; /* 호버 시 색상 전환 효과 */
}

.pagination button:hover {
  color: orange; /* 호버 시 색상 변경 */
}

.pagination span {
  color: white; /* 페이지 정보 글씨 색상 흰색 */
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 0.5rem; /* 선택한 태그와 검색 영역 간의 간격 조절 */
  padding-left: 7rem;
}

.selected-tag {
  background-color: orange;
  color: white;
  border-radius: 5px;
  padding: 5px 10px;
  margin-right: 10px;
  margin-bottom: 5px; /* 아래쪽 여백 추가 */
}

.remove-tag {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  margin-left: 5px;
  font-weight: bold;
}

.reset {
  margin-left: 10px;
}

</style>
