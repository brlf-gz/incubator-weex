import Base from '../base'
import { validateStyles } from '../../validator'
import { throttle, bind, createMixin } from '../../utils'
import indicator from './indicator'
import * as eventMethods from '../../methods/event'
import slideMixin from './slideMixin'

export default Base.extend({
  mixins: [createMixin(eventMethods), slideMixin],
  // components: { indicator },
  props: {
    'auto-play': {
      type: [String, Boolean],
      default: false
    },
    interval: {
      type: [String, Number],
      default: 3000
    }
  },

  data () {
    return {
      currentIndex: 0,
      frameCount: 0
    }
  },

  methods: {
    computeWrapperSize () {
      const wrapper = this.$refs.wrapper
      if (wrapper) {
        const rect = wrapper.getBoundingClientRect()
        this.wrapperWidth = rect.width
        this.wrapperHeight = rect.height
      }
    },

    updateLayout () {
      this.computeWrapperSize()
      const inner = this.$refs.inner
      if (inner) {
        inner.style.width = this.wrapperWidth * this.frameCount + 'px'
      }
    },

    formatChildren (createElement) {
      const children = this.$slots.default || []
      return children.filter(vnode => {
        // console.log(vnode)
        if (!vnode.tag) return false
        if (vnode.componentOptions && vnode.componentOptions.tag === 'indicator') {
          // console.log(vnode)
          // console.trace()
          this._indicator = createElement(indicator, {
            staticClass: vnode.data.staticClass,
            staticStyle: vnode.data.staticStyle,
            attrs: {
              count: this.frameCount,
              active: this.currentIndex
            }
          })
          return false
        }
        return true
      }).map(vnode => {
        return createElement('li', {
          staticClass: 'weex-slider-cell'
        }, [vnode])
      })
    }
  },

  created () {
    this._indicator = null
    this.$nextTick(() => {
      this.updateLayout()
    })
  },

  mounted () {
    if (this.autoPlay) {
      const interval = Number(this.interval)
      this._lastSlideTime = Date.now()

      const autoPlayFn = bind(function () {
        clearTimeout(this._autoPlayTimer)
        const now = Date.now()
        let nextTick = interval - now + this._lastSlideTime
        nextTick = nextTick > 100 ? nextTick : interval

        this.next()
        this._lastSlideTime = now
        this._autoPlayTimer = setTimeout(autoPlayFn, nextTick)
      }, this)

      this._autoPlayTimer = setTimeout(autoPlayFn, interval)
    }
  },

  render (createElement) {
    /* istanbul ignore next */
    if (process.env.NODE_ENV === 'development') {
      validateStyles('slider', this.$vnode.data && this.$vnode.data.staticStyle)
    }

    const innerElements = this.formatChildren(createElement)
    this.frameCount = innerElements.length

    return createElement(
      'nav',
      {
        ref: 'wrapper',
        attrs: { 'weex-type': 'slider' },
        staticClass: 'weex-slider weex-slider-wrapper',
        on: {
          touchstart: this.handleTouchStart,
          touchmove: throttle(bind(this.handleTouchMove, this), 25),
          touchend: this.handleTouchEnd
        }
      },
      [
        createElement('ul', {
          ref: 'inner',
          staticClass: 'weex-slider-inner'
        }, innerElements),
        this._indicator
      ]
    )
  }
})
