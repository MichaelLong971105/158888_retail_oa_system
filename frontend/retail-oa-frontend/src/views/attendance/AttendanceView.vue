<template>
  <div class="attendance-page">
    <div class="page-header">
      <div>
        <h3>Attendance Center</h3>
        <p>Manage weekly schedules, leave approval, punch records, and payroll-ready hours.</p>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="selectedWeek"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="Week start"
          @change="handleWeekChange"
        />
        <el-button type="primary" :loading="pageLoading" @click="refreshAll">
          Refresh
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Selected Employee</div>
          <div class="summary-value compact">{{ selectedEmployeeLabel }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Scheduled Hours</div>
          <div class="summary-value">{{ formatHours(myWeeklySummary.scheduledHours) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Actual Hours</div>
          <div class="summary-value">{{ formatHours(myWeeklySummary.actualHours) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Payable Hours</div>
          <div class="summary-value">{{ formatHours(myWeeklySummary.payableHours) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="attendance-tabs">
      <el-tab-pane label="Weekly Schedule" name="schedule">
        <el-row :gutter="16">
          <el-col :xs="24" :xl="15">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Schedule Board</span>
                  <div class="toolbar-inline">
                    <el-select
                      v-if="canManageAttendance"
                      v-model="selectedEmployeeId"
                      placeholder="Employee"
                      filterable
                      style="width: 220px"
                      @change="loadScheduleAndSummary"
                    >
                      <el-option
                        v-for="employee in employeeOptions"
                        :key="employee.id"
                        :label="employee.username"
                        :value="employee.id"
                      />
                    </el-select>
                    <el-tag type="info">{{ weekRangeLabel }}</el-tag>
                  </div>
                </div>
              </template>

              <el-table :data="scheduleRows" v-loading="scheduleLoading" style="width: 100%">
                <el-table-column prop="label" label="Day" width="140" />
                <el-table-column label="Shift" width="140">
                  <template #default="{ row }">
                    <el-select
                      v-model="row.shiftType"
                      :disabled="!canManageAttendance"
                      style="width: 100%"
                      @change="handleShiftTypeChange(row)"
                    >
                      <el-option label="Work" value="WORK" />
                      <el-option label="Rest" value="REST" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="Start" width="150">
                  <template #default="{ row }">
                    <el-time-picker
                      v-model="row.startTime"
                      :disabled="!canManageAttendance || row.shiftType === 'REST'"
                      value-format="HH:mm:ss"
                      placeholder="Start"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="End" width="150">
                  <template #default="{ row }">
                    <el-time-picker
                      v-model="row.endTime"
                      :disabled="!canManageAttendance || row.shiftType === 'REST'"
                      value-format="HH:mm:ss"
                      placeholder="End"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="Break" width="120">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.breakMinutes"
                      :disabled="!canManageAttendance || row.shiftType === 'REST'"
                      :min="0"
                      :step="15"
                      style="width: 100%"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="Note" min-width="220">
                  <template #default="{ row }">
                    <el-input
                      v-model="row.note"
                      :disabled="!canManageAttendance"
                      placeholder="Optional note"
                    />
                  </template>
                </el-table-column>
              </el-table>

              <div v-if="canManageAttendance" class="panel-footer">
                <el-button type="primary" :loading="savingSchedule" @click="saveSchedule">
                  Save Weekly Schedule
                </el-button>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :xl="9">
            <el-card class="side-panel">
              <template #header>
                <div class="card-header">
                  <span>Week Summary</span>
                </div>
              </template>

              <div class="metric-list">
                <div class="metric-item">
                  <span>Scheduled Hours</span>
                  <strong>{{ formatHours(myWeeklySummary.scheduledHours) }}</strong>
                </div>
                <div class="metric-item">
                  <span>Actual Hours</span>
                  <strong>{{ formatHours(myWeeklySummary.actualHours) }}</strong>
                </div>
                <div class="metric-item">
                  <span>Approved Leave</span>
                  <strong>{{ formatHours(myWeeklySummary.approvedLeaveHours) }}</strong>
                </div>
                <div class="metric-item">
                  <span>Payroll Hours</span>
                  <strong>{{ formatHours(myWeeklySummary.payableHours) }}</strong>
                </div>
              </div>
            </el-card>

            <el-card v-if="canManageAttendance" class="side-panel">
              <template #header>
                <div class="card-header">
                  <span>Team Weekly Payroll</span>
                </div>
              </template>

              <el-table :data="teamSummary" size="small" max-height="420" v-loading="teamSummaryLoading">
                <el-table-column prop="employeeUsername" label="Employee" min-width="140" />
                <el-table-column prop="scheduledHours" label="Scheduled" width="110">
                  <template #default="{ row }">{{ formatHours(row.scheduledHours) }}</template>
                </el-table-column>
                <el-table-column prop="actualHours" label="Actual" width="100">
                  <template #default="{ row }">{{ formatHours(row.actualHours) }}</template>
                </el-table-column>
                <el-table-column prop="payableHours" label="Payable" width="100">
                  <template #default="{ row }">{{ formatHours(row.payableHours) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="Leave Requests" name="leave">
        <el-row :gutter="16">
          <el-col :xs="24" :xl="10">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Submit Leave Request</span>
                </div>
              </template>

              <el-form label-width="110px">
                <el-form-item label="Approver">
                  <el-select v-model="leaveForm.approverId" clearable placeholder="Optional approver" style="width: 100%">
                    <el-option
                      v-for="approver in approverOptions"
                      :key="approver.id"
                      :label="`${approver.username} (${approver.role})`"
                      :value="approver.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="Leave Type">
                  <el-select v-model="leaveForm.leaveType" style="width: 100%">
                    <el-option v-for="item in leaveTypeOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Time Range">
                  <el-date-picker
                    v-model="leaveForm.range"
                    type="datetimerange"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    start-placeholder="Start time"
                    end-placeholder="End time"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="Reason">
                  <el-input v-model="leaveForm.reason" type="textarea" :rows="4" maxlength="300" show-word-limit />
                </el-form-item>
              </el-form>

              <div class="panel-footer">
                <el-button type="primary" :loading="submittingLeave" @click="submitLeaveRequest">
                  Submit Request
                </el-button>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :xl="14">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>My Leave Requests</span>
                </div>
              </template>

              <el-table :data="myLeaveRequests" v-loading="leaveLoading" style="width: 100%">
                <el-table-column prop="leaveType" label="Type" width="110" />
                <el-table-column prop="startTime" label="Start Time" width="180" />
                <el-table-column prop="endTime" label="End Time" width="180" />
                <el-table-column prop="approverUsername" label="Approver" width="140">
                  <template #default="{ row }">{{ row.approverUsername || '-' }}</template>
                </el-table-column>
                <el-table-column prop="reason" label="Reason" min-width="220" show-overflow-tooltip />
                <el-table-column label="Status" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getLeaveStatusType(row.status)">{{ row.status }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="Actions" width="120" fixed="right">
                  <template #default="{ row }">
                    <el-button
                      v-if="row.status === 'PENDING'"
                      link
                      type="danger"
                      @click="cancelLeave(row)"
                    >
                      Cancel
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane v-if="canApproveLeave" label="Approvals" name="approvals">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Pending Leave Approvals</span>
              <el-button size="small" @click="loadPendingApprovals">Refresh Queue</el-button>
            </div>
          </template>

          <el-table :data="pendingApprovals" v-loading="approvalsLoading" style="width: 100%">
            <el-table-column prop="applicantUsername" label="Employee" width="140" />
            <el-table-column prop="leaveType" label="Type" width="110" />
            <el-table-column prop="startTime" label="Start Time" width="180" />
            <el-table-column prop="endTime" label="End Time" width="180" />
            <el-table-column prop="reason" label="Reason" min-width="240" show-overflow-tooltip />
            <el-table-column prop="approverUsername" label="Assigned To" width="140">
              <template #default="{ row }">{{ row.approverUsername || 'Unassigned' }}</template>
            </el-table-column>
            <el-table-column label="Actions" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openApprovalDialog(row, 'approve')">Approve</el-button>
                <el-button link type="danger" @click="openApprovalDialog(row, 'reject')">Reject</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="Punch Records" name="punch">
        <el-row :gutter="16">
          <el-col :xs="24" :xl="16">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>Punch History</span>
                  <div class="toolbar-inline">
                    <el-select
                      v-if="canManageAttendance"
                      v-model="punchFilters.employeeId"
                      placeholder="Employee"
                      style="width: 220px"
                    >
                      <el-option
                        v-for="employee in employeeOptions"
                        :key="employee.id"
                        :label="employee.username"
                        :value="employee.id"
                      />
                    </el-select>
                    <el-date-picker
                      v-model="punchFilters.range"
                      type="daterange"
                      value-format="YYYY-MM-DD"
                      start-placeholder="Start date"
                      end-placeholder="End date"
                    />
                    <el-button type="primary" @click="loadPunchRecords">Search</el-button>
                  </div>
                </div>
              </template>

              <el-table :data="punchRecords" v-loading="punchLoading" style="width: 100%">
                <el-table-column prop="employeeUsername" label="Employee" width="140" />
                <el-table-column prop="punchType" label="Type" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.punchType === 'CLOCK_IN' ? 'success' : 'warning'">
                      {{ row.punchType }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="source" label="Source" width="120" />
                <el-table-column prop="punchTime" label="Punch Time" width="180" />
                <el-table-column prop="deviceCode" label="Device" width="130">
                  <template #default="{ row }">{{ row.deviceCode || '-' }}</template>
                </el-table-column>
                <el-table-column prop="externalRecordId" label="External ID" width="160">
                  <template #default="{ row }">{{ row.externalRecordId || '-' }}</template>
                </el-table-column>
                <el-table-column prop="rawPayload" label="Payload" min-width="220" show-overflow-tooltip />
              </el-table>
            </el-card>
          </el-col>

          <el-col v-if="canManageAttendance" :xs="24" :xl="8">
            <el-card class="side-panel">
              <template #header>
                <div class="card-header">
                  <span>Manual Punch Entry</span>
                </div>
              </template>

              <el-form label-width="110px">
                <el-form-item label="Employee">
                  <el-select v-model="manualPunchForm.employeeId" filterable style="width: 100%">
                    <el-option
                      v-for="employee in employeeOptions"
                      :key="employee.id"
                      :label="employee.username"
                      :value="employee.id"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="Type">
                  <el-select v-model="manualPunchForm.punchType" style="width: 100%">
                    <el-option label="CLOCK_IN" value="CLOCK_IN" />
                    <el-option label="CLOCK_OUT" value="CLOCK_OUT" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Time">
                  <el-date-picker
                    v-model="manualPunchForm.punchTime"
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="Source">
                  <el-select v-model="manualPunchForm.source" style="width: 100%">
                    <el-option label="MANUAL" value="MANUAL" />
                    <el-option label="API" value="API" />
                    <el-option label="MACHINE" value="MACHINE" />
                  </el-select>
                </el-form-item>
                <el-form-item label="Device Code">
                  <el-input v-model="manualPunchForm.deviceCode" />
                </el-form-item>
                <el-form-item label="External ID">
                  <el-input v-model="manualPunchForm.externalRecordId" />
                </el-form-item>
              </el-form>

              <div class="panel-footer">
                <el-button type="primary" :loading="submittingPunch" @click="submitManualPunch">
                  Save Punch Record
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="approvalDialog.visible" :title="approvalDialog.mode === 'approve' ? 'Approve Leave' : 'Reject Leave'" width="520px">
      <el-form label-width="100px">
        <el-form-item label="Employee">
          <span>{{ approvalDialog.record?.applicantUsername }}</span>
        </el-form-item>
        <el-form-item label="Time">
          <div class="dialog-text">
            {{ approvalDialog.record?.startTime }} to {{ approvalDialog.record?.endTime }}
          </div>
        </el-form-item>
        <el-form-item label="Comment">
          <el-input v-model="approvalDialog.comment" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approvalDialog.visible = false">Cancel</el-button>
        <el-button type="primary" :loading="approvalSubmitting" @click="submitApprovalDecision">
          Confirm
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  approveLeaveRequest,
  cancelLeaveRequest,
  createLeaveRequest,
  getAttendanceApprovers,
  getAttendanceEmployees,
  getMyLeaveRequests,
  getPendingLeaveRequests,
  getPunchRecords,
  getTeamWeeklySummary,
  getWeeklySchedule,
  getWeeklySummary,
  ingestPunchRecord,
  rejectLeaveRequest,
  upsertWeeklySchedule
} from '../../api/attendance'
import { authState, canAccess } from '../../utils/auth'

const leaveTypeOptions = ['ANNUAL', 'SICK', 'PERSONAL', 'OTHER']
const weekdayLabels = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday']

const pageLoading = ref(false)
const scheduleLoading = ref(false)
const savingSchedule = ref(false)
const leaveLoading = ref(false)
const submittingLeave = ref(false)
const approvalsLoading = ref(false)
const approvalSubmitting = ref(false)
const punchLoading = ref(false)
const submittingPunch = ref(false)
const teamSummaryLoading = ref(false)
const activeTab = ref('schedule')
const selectedWeek = ref(getWeekStart(new Date()))
const selectedEmployeeId = ref(null)
const employeeOptions = ref([])
const approverOptions = ref([])
const scheduleRows = ref(createEmptyScheduleRows(selectedWeek.value))
const myLeaveRequests = ref([])
const pendingApprovals = ref([])
const punchRecords = ref([])
const teamSummary = ref([])
const myWeeklySummary = ref({
  scheduledHours: 0,
  actualHours: 0,
  approvedLeaveHours: 0,
  payableHours: 0
})

const canManageAttendance = canAccess(['ADMIN', 'MANAGER'], ['MANAGE_ATTENDANCE'])
const canApproveLeave = canAccess(['ADMIN', 'MANAGER'], ['APPROVE_LEAVE', 'MANAGE_ATTENDANCE'])

function parseDateInput(dateInput) {
  if (dateInput instanceof Date) {
    return new Date(dateInput.getFullYear(), dateInput.getMonth(), dateInput.getDate())
  }

  if (typeof dateInput === 'string') {
    const [year, month, day] = dateInput.split('-').map(Number)
    return new Date(year, month - 1, day)
  }

  const date = new Date(dateInput)
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getWeekStart(dateInput) {
  const date = parseDateInput(dateInput)
  const day = date.getDay()
  const diff = day === 0 ? -6 : 1 - day
  date.setDate(date.getDate() + diff)
  return formatDate(date)
}

function getDateOffset(dateString, offset) {
  const date = parseDateInput(dateString)
  date.setDate(date.getDate() + offset)
  return formatDate(date)
}

function createEmptyScheduleRows(weekStart) {
  return weekdayLabels.map((label, index) => ({
    label,
    workDate: getDateOffset(weekStart, index),
    shiftType: 'REST',
    startTime: null,
    endTime: null,
    breakMinutes: 0,
    note: ''
  }))
}

const leaveForm = reactive({
  approverId: null,
  leaveType: 'ANNUAL',
  range: [],
  reason: ''
})

const manualPunchForm = reactive({
  employeeId: null,
  punchType: 'CLOCK_IN',
  punchTime: '',
  source: 'MANUAL',
  deviceCode: '',
  externalRecordId: ''
})

const punchFilters = reactive({
  employeeId: null,
  range: [selectedWeek.value, getDateOffset(selectedWeek.value, 6)]
})

const approvalDialog = reactive({
  visible: false,
  mode: 'approve',
  record: null,
  comment: ''
})

const selectedEmployeeLabel = computed(() => {
  const employee = employeeOptions.value.find(item => item.id === selectedEmployeeId.value)
  return employee?.username || authState.user?.username || '-'
})

const weekRangeLabel = computed(() => `${selectedWeek.value} to ${getDateOffset(selectedWeek.value, 6)}`)

function formatHours(value) {
  const amount = Number(value || 0)
  return amount.toFixed(2)
}

function getLeaveStatusType(status) {
  if (status === 'APPROVED') {
    return 'success'
  }

  if (status === 'REJECTED' || status === 'CANCELLED') {
    return 'danger'
  }

  return 'warning'
}

function handleShiftTypeChange(row) {
  if (row.shiftType === 'REST') {
    row.startTime = null
    row.endTime = null
    row.breakMinutes = 0
  } else {
    row.startTime = row.startTime || '09:00:00'
    row.endTime = row.endTime || '18:00:00'
  }
}

function resetLeaveForm() {
  leaveForm.approverId = null
  leaveForm.leaveType = 'ANNUAL'
  leaveForm.range = []
  leaveForm.reason = ''
}

function resetManualPunchForm() {
  manualPunchForm.employeeId = selectedEmployeeId.value
  manualPunchForm.punchType = 'CLOCK_IN'
  manualPunchForm.punchTime = ''
  manualPunchForm.source = 'MANUAL'
  manualPunchForm.deviceCode = ''
  manualPunchForm.externalRecordId = ''
}

async function loadBaseOptions() {
  const [employeesResponse, approversResponse] = await Promise.all([
    getAttendanceEmployees(),
    getAttendanceApprovers()
  ])

  employeeOptions.value = employeesResponse.data || []
  approverOptions.value = approversResponse.data || []

  if (!selectedEmployeeId.value) {
    selectedEmployeeId.value = employeeOptions.value[0]?.id || authState.user?.id || null
  }

  if (!punchFilters.employeeId) {
    punchFilters.employeeId = selectedEmployeeId.value
  }

  if (!manualPunchForm.employeeId) {
    manualPunchForm.employeeId = selectedEmployeeId.value
  }
}

async function loadScheduleAndSummary() {
  if (!selectedEmployeeId.value) {
    scheduleRows.value = createEmptyScheduleRows(selectedWeek.value)
    return
  }

  scheduleLoading.value = true
  try {
    const [scheduleResponse, summaryResponse] = await Promise.all([
      getWeeklySchedule({
        employeeId: selectedEmployeeId.value,
        weekStartDate: selectedWeek.value
      }),
      getWeeklySummary({
        employeeId: selectedEmployeeId.value,
        weekStartDate: selectedWeek.value
      })
    ])

    const scheduleMap = new Map((scheduleResponse.data || []).map(item => [item.workDate, item]))
    scheduleRows.value = createEmptyScheduleRows(selectedWeek.value).map(row => {
      const saved = scheduleMap.get(row.workDate)
      if (!saved) {
        return row
      }

      return {
        ...row,
        shiftType: saved.shiftType,
        startTime: saved.startTime,
        endTime: saved.endTime,
        breakMinutes: saved.breakMinutes,
        note: saved.note || ''
      }
    })

    myWeeklySummary.value = summaryResponse.data || myWeeklySummary.value
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load weekly schedule')
  } finally {
    scheduleLoading.value = false
  }
}

async function saveSchedule() {
  if (!selectedEmployeeId.value) {
    ElMessage.warning('Please select an employee')
    return
  }

  savingSchedule.value = true

  try {
    await upsertWeeklySchedule({
      employeeId: selectedEmployeeId.value,
      weekStartDate: selectedWeek.value,
      shifts: scheduleRows.value.map(row => ({
        workDate: row.workDate,
        shiftType: row.shiftType,
        startTime: row.shiftType === 'WORK' ? row.startTime : null,
        endTime: row.shiftType === 'WORK' ? row.endTime : null,
        breakMinutes: row.shiftType === 'WORK' ? Number(row.breakMinutes || 0) : 0,
        note: row.note?.trim() || null
      }))
    })

    ElMessage.success('Weekly schedule saved')
    await Promise.all([loadScheduleAndSummary(), loadTeamSummary()])
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to save weekly schedule')
  } finally {
    savingSchedule.value = false
  }
}

async function loadMyLeaveRequests() {
  leaveLoading.value = true
  try {
    const response = await getMyLeaveRequests()
    myLeaveRequests.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load leave requests')
  } finally {
    leaveLoading.value = false
  }
}

async function submitLeaveRequest() {
  if (!leaveForm.range?.length || !leaveForm.reason.trim()) {
    ElMessage.warning('Please complete the leave request form')
    return
  }

  submittingLeave.value = true
  try {
    await createLeaveRequest({
      approverId: leaveForm.approverId,
      leaveType: leaveForm.leaveType,
      startTime: leaveForm.range[0],
      endTime: leaveForm.range[1],
      reason: leaveForm.reason.trim()
    })

    ElMessage.success('Leave request submitted')
    resetLeaveForm()
    await Promise.all([loadMyLeaveRequests(), canApproveLeave ? loadPendingApprovals() : Promise.resolve(), loadScheduleAndSummary(), loadTeamSummary()])
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to submit leave request')
  } finally {
    submittingLeave.value = false
  }
}

async function cancelLeave(row) {
  try {
    await ElMessageBox.confirm('Cancel this pending leave request?', 'Confirm', { type: 'warning' })
    await cancelLeaveRequest(row.id)
    ElMessage.success('Leave request cancelled')
    await Promise.all([loadMyLeaveRequests(), loadScheduleAndSummary()])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.error || 'Failed to cancel leave request')
    }
  }
}

async function loadPendingApprovals() {
  if (!canApproveLeave) {
    return
  }

  approvalsLoading.value = true
  try {
    const response = await getPendingLeaveRequests()
    pendingApprovals.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load approval queue')
  } finally {
    approvalsLoading.value = false
  }
}

function openApprovalDialog(record, mode) {
  approvalDialog.visible = true
  approvalDialog.mode = mode
  approvalDialog.record = record
  approvalDialog.comment = ''
}

async function submitApprovalDecision() {
  if (!approvalDialog.record) {
    return
  }

  approvalSubmitting.value = true
  try {
    const payload = {
      comment: approvalDialog.comment?.trim() || null
    }

    if (approvalDialog.mode === 'approve') {
      await approveLeaveRequest(approvalDialog.record.id, payload)
      ElMessage.success('Leave request approved')
    } else {
      await rejectLeaveRequest(approvalDialog.record.id, payload)
      ElMessage.success('Leave request rejected')
    }

    approvalDialog.visible = false
    await Promise.all([loadPendingApprovals(), loadMyLeaveRequests(), loadScheduleAndSummary(), loadTeamSummary()])
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to update leave request')
  } finally {
    approvalSubmitting.value = false
  }
}

async function loadPunchRecords() {
  punchLoading.value = true
  try {
    const response = await getPunchRecords({
      employeeId: canManageAttendance ? punchFilters.employeeId : selectedEmployeeId.value,
      startDate: punchFilters.range?.[0],
      endDate: punchFilters.range?.[1]
    })
    punchRecords.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load punch records')
  } finally {
    punchLoading.value = false
  }
}

async function submitManualPunch() {
  if (!manualPunchForm.employeeId || !manualPunchForm.punchTime) {
    ElMessage.warning('Please complete the manual punch form')
    return
  }

  submittingPunch.value = true
  try {
    await ingestPunchRecord({
      employeeId: manualPunchForm.employeeId,
      punchType: manualPunchForm.punchType,
      punchTime: manualPunchForm.punchTime,
      source: manualPunchForm.source,
      deviceCode: manualPunchForm.deviceCode?.trim() || null,
      externalRecordId: manualPunchForm.externalRecordId?.trim() || null,
      rawPayload: null
    })

    ElMessage.success('Punch record saved')
    resetManualPunchForm()
    await Promise.all([loadPunchRecords(), loadScheduleAndSummary(), loadTeamSummary()])
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to save punch record')
  } finally {
    submittingPunch.value = false
  }
}

async function loadTeamSummary() {
  if (!canManageAttendance) {
    return
  }

  teamSummaryLoading.value = true
  try {
    const response = await getTeamWeeklySummary({
      weekStartDate: selectedWeek.value
    })
    teamSummary.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load team payroll summary')
  } finally {
    teamSummaryLoading.value = false
  }
}

async function refreshAll() {
  pageLoading.value = true
  try {
    await loadBaseOptions()
    await Promise.all([
      loadScheduleAndSummary(),
      loadMyLeaveRequests(),
      loadPunchRecords(),
      canApproveLeave ? loadPendingApprovals() : Promise.resolve(),
      canManageAttendance ? loadTeamSummary() : Promise.resolve()
    ])
  } finally {
    pageLoading.value = false
  }
}

function handleWeekChange(value) {
  selectedWeek.value = getWeekStart(value || new Date())
  scheduleRows.value = createEmptyScheduleRows(selectedWeek.value)
  punchFilters.range = [selectedWeek.value, getDateOffset(selectedWeek.value, 6)]
  refreshAll()
}

onMounted(async () => {
  try {
    await refreshAll()
    resetLeaveForm()
    resetManualPunchForm()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to initialize attendance center')
  }
})
</script>

<style scoped>
.attendance-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header,
.card-header,
.header-actions,
.toolbar-inline,
.panel-footer {
  display: flex;
  align-items: center;
}

.page-header,
.card-header {
  justify-content: space-between;
  gap: 16px;
}

.page-header h3 {
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #606266;
}

.header-actions,
.toolbar-inline,
.panel-footer {
  gap: 12px;
  flex-wrap: wrap;
}

.summary-row {
  margin: 0;
}

.summary-card {
  min-height: 116px;
}

.summary-label {
  color: #909399;
  font-size: 13px;
}

.summary-value {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 700;
  color: #303133;
}

.summary-value.compact {
  font-size: 20px;
  line-height: 1.35;
}

.attendance-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.side-panel {
  height: fit-content;
}

.metric-list {
  display: grid;
  gap: 12px;
}

.metric-item {
  background: #f7f8fa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.metric-item span {
  color: #606266;
}

.metric-item strong {
  font-size: 20px;
  color: #303133;
}

.dialog-text {
  color: #303133;
}

@media (max-width: 1200px) {
  .page-header,
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
